plugins {
    id("asoft-lib-browser-mpp")
}

kotlin.sourceSets["jsMain"].dependencies {
    api(kotlin("stdlib-js"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${versions.kotlinx.coroutines}")
    api("org.jetbrains.kotlinx:kotlinx-html-js:${versions.kotlinx.html}")
    api("org.jetbrains:kotlin-extensions:${versions.kotlinjs.extensions}")
    api("org.jetbrains:kotlin-react:${versions.kotlinjs.react}")
    api("org.jetbrains:kotlin-styled:${versions.kotlinjs.styled}")
    api("org.jetbrains:kotlin-css-js:${versions.kotlinjs.css}")
    api("org.jetbrains:kotlin-react-router-dom:${versions.kotlinjs.reactRouterDom}")
    api(project(":tools"))
}