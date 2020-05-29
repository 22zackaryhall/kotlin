plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("android-target")
    id("bintray-upload-multiplatform")
}

kotlin {
    android {
        compilations.all {
            kotlinOptions { jvmTarget = "1.8" }
        }
        publishLibraryVariants("release")
    }

    jvm {
        compilations.all {
            kotlinOptions { jvmTarget = "1.8" }
        }
    }

    js {
        useCommonJs()
    }
}