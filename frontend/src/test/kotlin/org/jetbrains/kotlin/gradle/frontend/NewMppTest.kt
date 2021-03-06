package org.jetbrains.kotlin.gradle.frontend

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.jetbrains.kotlin.gradle.frontend.util.mkdirsOrFail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewMppTest {
    private val gradleVersion: String = "6.1.1"//"4.10.3"
    private val kotlinVersion: String = "1.3.70"

    private val port = 8098
    private val builder = BuildScriptBuilder()

    private lateinit var settingsGradleFile: File
    private lateinit var buildGradleFile: File
    private lateinit var runner: GradleRunner

    @get:Rule
    val testName = TestName()

    @get:Rule
    val failedRule = object : TestWatcher() {
        override fun failed(e: Throwable?, description: Description?) {
            val dst = File(
                "build/tests/${testName.methodName.replace("[", "-").replace("]", "")}"
            ).apply { mkdirsOrFail() }
            projectDir.root.copyRecursively(dst, true) { file, copyError ->
                System.err.println("Failed to copy $file due to ${copyError.message}")
                OnErrorAction.SKIP
            }
            println("Copied project to ${dst.absolutePath}")
        }

        /*
        // useful for debugging
        override fun succeeded(description: Description?) {
            failed(null, description)
        }
        // */
    }

    @get:Rule
    val projectDir = TemporaryFolder()

    val D = "$"

    fun File.makeParentsAndWriteText(text: String) {
        parentFile.mkdirsOrFail()
        writeText(text)
    }

    @Before
    fun setUp() {
        projectDir.create()
        projectDir.root.resolve("build/kotlin-build/caches").mkdirsOrFail()

        buildGradleFile = projectDir.root.resolve("build.gradle")
        settingsGradleFile = projectDir.root.resolve("settings.gradle")

        projectDir.root.resolve("src/commonMain/kotlin/sample/Sample.kt")
            .makeParentsAndWriteText("expect fun f(): Int")
        projectDir.root.resolve("src/commonTest/kotlin/sample/SampleTests.kt")
            .makeParentsAndWriteText(
                """
import kotlin.test.*

class SampleTests {
    @Test
    fun testMe() {
        assertTrue(f() > 0)
    }
}
                """
            )

        projectDir.root.resolve("src/jsMain/kotlin/sample/SampleJs.kt")
            .makeParentsAndWriteText("actual fun f(): Int = 1")
        projectDir.root.resolve("src/jsTest/kotlin/sample/SampleTestsJs.kt")
            .makeParentsAndWriteText(
                """
import kotlin.test.*

class SampleTestsJs {
    @Test
    fun testMe() {
        assertTrue(f() == 1)
    }
}
                """
            )

        projectDir.root.resolve("src/jvmMain/kotlin/sample/SampleJvm.kt")
            .makeParentsAndWriteText("actual fun f(): Int = 2")
        projectDir.root.resolve("src/jsMain/kotlin/sample/index.kt").makeParentsAndWriteText(
            """
            fun main() { console.log("does work") }
        """.trimIndent()
        )
        projectDir.root.resolve("src/jsMain/resources/index.html").makeParentsAndWriteText(
            """
                <html>
                    <head>Sample Test</head>
                    <body><h1>works</h1></body>
                    <script src="main.bundle.js"></script>
                </html>
            """.trimIndent()
        )
        projectDir.root.resolve("src/jvmTest/kotlin/sample/SampleTestsJvm.kt")
            .makeParentsAndWriteText(
                """
import kotlin.test.*

class SampleTestsJvm {
    @Test
    fun testMe() {
        assertTrue(f() == 2)
    }
}
                """
            )

        buildGradleFile.parentFile.mkdirsOrFail()
        settingsGradleFile.parentFile.mkdirsOrFail()

        runner = GradleRunner.create()
            .withProjectDir(projectDir.root)
            .withPluginClasspath()
            .withGradleVersion(gradleVersion)

        settingsGradleFile.writeText(
            """
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlin-multiplatform") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:$D{requested.version}")
            }
        }
    }
    
    resolutionStrategy {
        eachPlugin {
//            if (requested.id.id == "kotlin-frontend") {
//                useModule('tz.co.asoft:kotlin-frontend:0.0.46')
//            }
        }
    }

    repositories {
        mavenCentral()
        maven { url 'https://plugins.gradle.org/m2/' }
    }
}

rootProject.name = 'new-mpp'
                """
        )
    }

    @Test
    fun testSimple() {
        buildGradleFile.writeText(
            """
plugins {
    id 'kotlin-multiplatform' version '$kotlinVersion'
    id 'tz.co.asoft.frontend'
}

apply plugin: "kotlin-dce-js"

repositories {
    jcenter()
    mavenCentral()
}

kotlin {
    js {
        compilations.all {
            tasks[compileKotlinTaskName].kotlinOptions {
                metaInfo = true
                sourceMap = true
                moduleKind = 'commonjs'
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation 'org.jetbrains.kotlin:kotlin-stdlib-common'
            }
        }
        commonTest {
            dependencies {
                implementation 'org.jetbrains.kotlin:kotlin-test-common'
                implementation 'org.jetbrains.kotlin:kotlin-test-annotations-common'
            }
        }
        jsMain {
            dependencies {
                implementation 'org.jetbrains.kotlin:kotlin-stdlib-js'
            }
        }
        jsTest {
            dependencies {
                implementation 'org.jetbrains.kotlin:kotlin-test-js'
            }
        }
    }
}

kotlinFrontend {
    npm {
        dependency("is-number","7.0.0")
//        devDependency("karma")
        webpackDependencies()
    }

    sourceMaps = false

    webpackBundle {
        bundleName = "main"
    }
}
                """
        )

        println("Running npm-install")
        runner.withArguments("npm-install").build().apply {
            println("Done running install")
            val nodeModules = projectDir.root.resolve("build/node_modules").listFiles()
            println("Installed ${nodeModules?.size} modules")
            nodeModules?.take(3)?.forEach { println(it.name) }
            assertEquals(true, nodeModules?.isNotEmpty())
            println(output)
            assertEquals(TaskOutcome.SUCCESS, task(":npm-install")?.outcome)
        }

        println("Running Bundle")
        runner.withArguments("bundle").build().apply {
            assertEquals(TaskOutcome.UP_TO_DATE, task(":npm-preunpack")?.outcome)
            assertEquals(TaskOutcome.UP_TO_DATE, task(":npm-install")?.outcome)
            assertEquals(TaskOutcome.SUCCESS, task(":webpack-config")?.outcome)
            assertEquals(TaskOutcome.SUCCESS, task(":webpack-bundle")?.outcome)
            assertTrue { projectDir.root.resolve("build/classes/kotlin/js/main/new-mpp.js").isFile }
            assertTrue { projectDir.root.resolve("build/bundle/main.bundle.js").isFile }
            println(output)
        }

        println("Running Run")
        runner.withArguments("run").build().apply {
            assertEquals(TaskOutcome.SUCCESS, task(":run")?.outcome)
            println(output)
        }

        println("Running Stop")
        runner.withArguments("stop").build().apply {
            assertEquals(TaskOutcome.SUCCESS, task(":stop")?.outcome)
            println(output)
        }
    }
}