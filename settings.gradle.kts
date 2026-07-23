plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "webstore-platform"
include("platform-internal-api", "platform-spring-webview", "webstore-view")
