plugins {
    id("maven-publish")
    id("com.jfrog.bintray")
}

bintray {
    configureBintray()
    setPublications("kotlinMultiplatform", "metadata", "androidRelease", "jvm", "js")
}