plugins {
    id("asoft-lib-jvm")
}

dependencies {
    api(project(":rest-controller-core"))
    api(project(":result"))
    api(project(":persist"))
    api(project(":tools-core"))
    api("io.ktor:ktor-server-cio:${versions.ktor}")
    api("io.ktor:ktor-network:${versions.ktor}")
}
