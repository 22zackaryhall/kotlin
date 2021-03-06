package org.jetbrains.kotlin.gradle.frontend.webpack

import groovy.json.*
import groovy.lang.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.frontend.npm.NpmExtension
import org.jetbrains.kotlin.gradle.frontend.util.*
import org.jetbrains.kotlin.gradle.tasks.*
import java.io.*
import java.util.ArrayDeque

/**
 * @author Sergey Mashkov
 */
open class GenerateWebPackConfigTask : DefaultTask() {
    @Input
    val projectDirectory: String = project.projectDir.absolutePath

    private val configsDir: File? = project.file("$projectDirectory/webpack.config.d")

    @get:Input
    val contextDir by lazy { kotlinOutput(project).parentFile.absolutePath!! }

    @get:Internal
    val bundles by lazy { project.frontendExtension.bundles().filterIsInstance<WebPackExtension>() }

    @get:Input
    val bundleNameInput: Any by lazy { bundles.singleOrNull()?.bundleName ?: "" }

    @get:Input
    val publicPathInput: Any by lazy { bundles.singleOrNull()?.publicPath ?: "" }

    @get:Input
    val outputFileName by lazy { kotlinOutput(project).name }

    @get:OutputDirectory
    val bundleDirectory by lazy {
        handleFile(project, project.frontendExtension.bundlesDirectory).apply { mkdirsOrFail() }
    }

    @OutputFile
    val webPackConfigFile: File = project.buildDir.resolve("webpack.config.js")

    @Input
    val defined = project.frontendExtension.defined

    @get:Input
    val isDceEnabled: Boolean by lazy {
        !project.tasks
            .withType(KotlinJsDce::class.java)
            .none { it.isEnabled }
    }

    init {
        if (configsDir?.exists() == true) {
            (inputs as TaskInputs).dir(configsDir).optional()
        }

        onlyIf {
            bundles.size == 1 && bundles.single().webpackConfigFile == null
        }
    }

    private val npm = project.extensions.getByType(NpmExtension::class.java)

    fun getModuleResolveRoots(testMode: Boolean): List<String> {
        val resolveRoots = mutableListOf<String>()

        val dceOutputFiles = project.tasks
            .withType(KotlinJsDce::class.java)
            .filter { it.isEnabled && !it.name.contains("test", ignoreCase = true) }
            .flatMap { it.outputs.files }

        if (dceOutputFiles.isEmpty() || testMode) {
            resolveRoots.add(getContextDir(testMode).toRelativeString(project.buildDir))

            // Recursively walk the dependency graph and build a set of transitive local projects.
            val allProjects = mutableSetOf<Project>()
            val queue = ArrayDeque<Project>().apply { add(project) }
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                val dependencies = current.configurations.findByName("compile")?.allDependencies
                    ?.filterIsInstance<ProjectDependency>().orEmpty()
                    .mapNotNull { it.dependencyProject }

                allProjects.addAll(dependencies)
                queue.addAll(dependencies)
            }

            allProjects.flatMap { it.tasks.filterIsInstance<Kotlin2JsCompile>() }
                .filter { !it.name.contains("test", ignoreCase = true) }
                .map { project.file(it.outputFileBridge()) }
                .forEach { resolveRoots.add(it.parentFile.toRelativeString(project.buildDir)) }
        } else {
            resolveRoots.addAll(dceOutputFiles.map { it.toRelativeString(project.buildDir) })
            resolveRoots.addAll(dceOutputFiles.map { it.absolutePath })
        }

        if (testMode) {
            resolveRoots.add(kotlinOutput(project).absolutePath)
        }

        val sourceSets: SourceSetContainer? =
            project.convention.findPlugin(JavaPluginConvention::class.java)?.sourceSets
        val mainSourceSet: SourceSet? = sourceSets?.findByName("main")
        val resources = mainSourceSet?.output?.resourcesDir

        if (resources != null) {
            resolveRoots.add(resources.toRelativeString(project.buildDir))
        }

        // node modules
        resolveRoots.add(project.buildDir.resolve("node_modules").absolutePath)
        resolveRoots.add(npm.nodeModulesDir.absolutePath)
        resolveRoots.add(npm.nodeModulesDir.toRelativeString(webPackConfigFile))
        resolveRoots.add(
            project.buildDir.resolve("node_modules").toRelativeString(project.buildDir)
        )

        return resolveRoots
    }

    fun getContextDir(testMode: Boolean): File {
        val dceOutputs = project.tasks
            .withType(KotlinJsDce::class.java)
            .filter { it.isEnabled && !it.name.contains("test", ignoreCase = true) }
            .map { it.destinationDir }
            .firstOrNull()

        return if (dceOutputs == null || testMode) kotlinOutput(project).parentFile.absoluteFile!!
        else dceOutputs.absoluteFile
    }

    private fun file(path: String): File? {
        val f = project.file(path)
        return if (f.exists()) f else null
    }

    @TaskAction
    fun generateConfig() {
        val bundle = bundles.singleOrNull()
            ?: throw GradleException("Only single webpack bundle supported")

        val resolveRoots = getModuleResolveRoots(false)

        val sourceSets: SourceSetContainer? =
            project.convention.findPlugin(JavaPluginConvention::class.java)?.sourceSets
        val mainSourceSet: SourceSet? = sourceSets?.findByName("main")
        val resources = mainSourceSet?.output?.resourcesDir

        val contentBase = setOf(
            bundle.contentPath.absolutePath,
            file("src/jsMain/resources")?.absolutePath,
            file("src/main/resources")?.absolutePath,
            file("build/processedResources/js/main")?.absolutePath,
            resources?.absolutePath
        ).mapNotNull { it }

//        val contentBase = listOf("build/processedResources/js/main")

        val json = linkedMapOf(
            "mode" to bundle.mode,
            "context" to getContextDir(false).absolutePath,
            "entry" to mapOf(
                bundle.bundleName to kotlinOutput(project).nameWithoutExtension.let { "./$it" }
            ),
            "output" to mapOf(
                "path" to bundleDirectory.absolutePath,
                "filename" to "[name].bundle.js",
                "chunkFilename" to "[id].bundle.js",
                "publicPath" to bundle.publicPath
            ),
            "devServer" to mapOf(
                "port" to bundle.port,
                "host" to "0.0.0.0",
                "historyApiFallback" to true,
                "contentBase" to contentBase
            ),
            "module" to mapOf(
                "rules" to listOf(
                    mapOf(
                        "test" to """*/\.css$/*""",
                        "use" to listOf("style-loader", "css-loader")
                    ),
                    mapOf(
                        "test" to """*/\.js$/*""",
                        "exclude" to "*/node_modules/*",
                        "use" to mapOf(
                            "loader" to "babel-loader",
                            "options" to mapOf(
                                "presets" to listOf("@babel/preset-env", "@babel/preset-react")
                            )
                        )
                    ),
                    mapOf(
                        "test" to """*/\.(jpe?g|png|gif|svg)$/*i""",
                        "loader" to "file-loader?name=app/images/[name].[ext]"
                    ),
                    mapOf(
                        "test" to """*/\.(woff(2)?|ttf|eot|svg)(\?v=\d+\.\d+\.\d+)?$/*""",
                        "use" to listOf(
                            mapOf(
                                "loader" to "file-loader",
                                "options" to mapOf(
                                    "name" to "[name].[ext]",
                                    "outputPath" to "fonts/"
                                )
                            )
                        )
                    )
                )
            ),
            "resolve" to mapOf(
                "modules" to resolveRoots + contentBase
            ),
            "resolveLoader" to mapOf(
                "modules" to listOf(npm.nodeModulesDir.absolutePath),
                "extensions" to listOf(".js", ".json"),
                "mainFields" to listOf("loader", "main")
            ),
            "plugins" to listOf<Any>()
        )

        webPackConfigFile.bufferedWriter().use { out ->
            out.appendln("'use strict';")
            out.append("var config = ")
            val configJson = JsonBuilder(json).toPrettyString()
                .replace("""/*i"""", "/i")
                .replace("""/*"""", "/")
                .replace(""""*/""", "/")
                .replace("\\\\", "\\")
            out.append(configJson)
            out.appendln(";")

            if (defined.isNotEmpty()) {
                out.append("var defined = ")
                out.append(JsonBuilder(defined).toPrettyString())
                out.appendln(";")
                out.appendln("config.plugins.push(new webpack.DefinePlugin(defined));")
            }

            out.appendln()
            out.appendln("module.exports = config;")
            out.appendln()

            val p = "^\\d+".toRegex()
            configsDir?.listFiles().orEmpty().sortedBy {
                p.find(it.nameWithoutExtension)?.value?.toInt() ?: 0
            }.forEach {
                out.appendln("// from file ${it.path}")
                it.reader().use {
                    it.copyTo(out)
                }
                out.appendln()
            }
        }
    }

    private fun Kotlin2JsCompile.outputFileBridge(): File {
        kotlinOptions.outputFile?.let { return project.file(it) }

        outputFile.javaClass.getMethod("getOutputFile")
            ?.let { return project.file(it.invoke(outputFile)) }

        throw IllegalStateException("Unable to locate kotlin js output file")
    }

    companion object {
        fun handleFile(project: Project, dir: Any): File {
            return when (dir) {
                is String -> File(dir).let { if (it.isAbsolute) it else project.buildDir.resolve(it) }
                is File -> dir
                is Function0<*> -> handleFile(
                    project, dir()
                        ?: throw IllegalArgumentException("function for webPackConfig.bundleDirectory shouldn't return null")
                )
                is Closure<*> -> handleFile(
                    project, dir.call()
                        ?: throw IllegalArgumentException("closure for webPackConfig.bundleDirectory shouldn't return null")
                )
                else -> project.file(dir)
            }
        }
    }
}
