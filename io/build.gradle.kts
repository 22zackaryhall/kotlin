plugins {
    id("asoft-lib")
}

kotlin.sourceSets {
    val commonMain by getting {
        dependencies {
            implementation(kotlin("stdlib-common"))
            api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:${versions.kotlinx.serialization}")
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:${versions.kotlinx.coroutines}")
            api(project(":klock"))
        }
    }

    val androidMain by getting {
        dependencies {
            implementation(kotlin("stdlib"))
            api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${versions.kotlinx.serialization}")
            api("org.jetbrains.kotlinx:kotlinx-coroutines-android:${versions.kotlinx.coroutines}")
        }
    }

    val jvmMain by getting {
        dependencies {
            implementation(kotlin("stdlib"))
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.kotlinx.coroutines}")
            api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${versions.kotlinx.serialization}")
        }
    }

    val jsMain by getting {
        dependencies {
            implementation(kotlin("stdlib-js"))
            api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:${versions.kotlinx.serialization}")
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${versions.kotlinx.coroutines}")
        }
    }
}