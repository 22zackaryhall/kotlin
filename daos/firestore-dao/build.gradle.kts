plugins {
    id("asoft-lib")
}

kotlin.sourceSets {
    val commonMain by getting {
        dependencies {
            api(project(":persist"))
            api(project(":firebase-firestore"))
        }
    }
}