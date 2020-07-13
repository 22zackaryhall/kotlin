pluginManagement {
    repositories {
        google()
        jcenter()
    }
}

includeBuild("../../build-src")
includeBuild("../../test")

include(":firebase-core")
project(":firebase-core").projectDir = File("../firebase-core")
include(":firebase-storage")
project(":firebase-storage").projectDir = File("../firebase-storage")
include(":firebase-firestore")
project(":firebase-firestore").projectDir = File("../firebase-firestore")
include(":tools")
project(":tools").projectDir = File("../../tools")
include(":persist")
project(":persist").projectDir = File("../../persist")
include(":platform")
project(":platform").projectDir = File("../../platform")
include(":klock")
project(":klock").projectDir = File("../../klock")
include(":io")
project(":io").projectDir = File("../../io")