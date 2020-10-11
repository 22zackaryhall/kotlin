plugins {
    id("asoft-lib")
}

kotlin.sourceSets {
    val commonMain by getting {
        dependencies {
            api(project(":krypto"))
            api(project(":klock"))
            api(project(":mapper"))
            api(project(":security-keys"))
        }
    }

    val commonTest by getting {
        dependencies {
            api(asoft("test"))
        }
    }
}
