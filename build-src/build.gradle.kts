plugins {
    `kotlin-dsl`
    id("java")
    id("maven-publish")
    id("java-gradle-plugin")
}

repositories {
    google()
    jcenter()
    mavenCentral()
    maven("https://maven.jetbrains.space/asofttz/kotlin") {
        credentials {
            username = System.getenv("SPACE_USERNAME")
            password = System.getenv("SPACE_PASSWORD")
        }
    }
}

object versions {
    const val kotlin = "1.3.72"
    const val asoft = "0.1.0-dev-16"

    object android {
        const val build_tools = "4.0.0" //"4.0.0"
    }
}

group = "tz.co.asoft"
version = versions.asoft

defaultTasks("jar")

val sourcesJar by tasks.creating(org.gradle.jvm.tasks.Jar::class) {
    archiveClassifier.value("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.creating(org.gradle.jvm.tasks.Jar::class) {
    archiveClassifier.value("javadoc")
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}

dependencies {
    api("com.android.tools.build:gradle:${versions.android.build_tools}")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}")
    api("org.jetbrains.kotlin:kotlin-serialization:${versions.kotlin}")
}

publishing {
    repositories {
        maven("https://maven.jetbrains.space/asofttz/kotlin") {
            credentials {
                username = System.getenv("SPACE_USERNAME")
                password = System.getenv("SPACE_PASSWORD")
            }
        }
    }
}
