package org.jetbrains.kotlin.gradle.frontend.webpack

import org.gradle.api.*
import org.gradle.language.jvm.tasks.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.frontend.*
import org.jetbrains.kotlin.gradle.frontend.util.*
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

object WebPackLauncher : Launcher {
    override fun apply(packageManager: PackageManager, project: Project,
                       packagesTask: Task, startTask: Task, stopTask: Task) {
        project.afterEvaluate {
            if (project.frontendExtension.bundles().any { it is WebPackExtension }) {
                try {
                    val run = project.tasks.create("webpack-run", WebpackDevServerStartTask::class.java) { t ->
                        t.description = "Start webpack dev server (if not yet running)"
                        t.group = WebPackBundler.WebPackGroup
                    }
                    val stop = project.tasks.create("webpack-stop", WebpackDevServerStopTask::class.java) { t ->
                        t.description = "Stop webpack dev server (if running)"
                        t.group = WebPackBundler.WebPackGroup
                    }

                    project.withTask(GenerateWebPackConfigTask::class) { task ->
                        run.dependsOn(task)
                    }
                    project.withTask(RelativizeSourceMapTask::class) { task ->
                        run.dependsOn(task)
                    }

                    project.withTask<KotlinJsCompile> { task ->
                        run.dependsOn(task)
                    }
                    project.withTask<KotlinJsDce> { task ->
                        run.dependsOn(task)
                    }
                    project.withTask<ProcessResources> { task ->
                        run.dependsOn(task)
                    }

                    run.dependsOn(packagesTask)

                    startTask.dependsOn(run)
                    stopTask.dependsOn(stop)
                } catch (c: Throwable) {
                    println(c.message)
                    logger.info(c.message)
                    throw c
                }
            }
        }
    }
}