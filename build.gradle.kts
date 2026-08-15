import java.util.zip.ZipFile

plugins {
    base
}

group = "org.tavall"
extra["versionTagPrefix"] = "Webstore"
extra["fallbackVersion"] = "1.0.0"
apply(from = "gradle/git-version.gradle.kts")
version = extra["gitVersion"] as String

val springBootVersion = "3.5.6"
val lombokVersion = "1.18.40"
val fabric8Version = "6.13.4"
val tavallToolsVersion = "1.0.0"

subprojects {
    group = rootProject.group
    version = rootProject.version
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
        withSourcesJar()
    }

    repositories {
        mavenCentral()
        val githubToken = providers.environmentVariable("GITHUB_TOKEN").orNull
        if (!githubToken.isNullOrBlank()) {
            listOf(
                "tavall-di",
                "tavall-cache",
                "tavall-concurrency",
                "tavall-database",
                "tavall-eventbus",
                "tavall-logging",
                "tavall-reflection",
                "tavall-registry",
                "tavall-scheduler",
            ).forEach { repository ->
                maven("https://maven.pkg.github.com/TavallStudios/$repository") {
                    name = "github${repository.replace("-", "")}"
                    credentials {
                        username = providers.environmentVariable("GITHUB_ACTOR").orElse("github").get()
                        password = githubToken
                    }
                }
            }
        }
    }

    dependencies {
        "implementation"(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
        "implementation"("org.tavall:tavall-di:$tavallToolsVersion")
        "testImplementation"(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
        "compileOnly"("org.projectlombok:lombok:$lombokVersion")
        "annotationProcessor"("org.projectlombok:lombok:$lombokVersion")
        "testCompileOnly"("org.projectlombok:lombok:$lombokVersion")
        "testAnnotationProcessor"("org.projectlombok:lombok:$lombokVersion")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    dependencyLocking {
        lockAllConfigurations()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release = 25
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("-Dnet.bytebuddy.experimental=true")
    }

    tasks.withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    val verifyThinJar = tasks.register("verifyThinJar") {
        dependsOn(tasks.named("jar"))
        val archive = tasks.named<Jar>("jar").flatMap { it.archiveFile }
        inputs.file(archive)
        doLast {
            val forbidden = listOf("com/fasterxml/", "io/fabric8/", "jakarta/", "org/hibernate/", "org/springframework/")
            ZipFile(archive.get().asFile).use { jar ->
                val embedded = jar.entries().asSequence().map { it.name }
                    .firstOrNull { entry -> forbidden.any(entry::startsWith) }
                check(embedded == null) { "Third-party class embedded in thin JAR: $embedded" }
            }
        }
    }
    tasks.named("check") {
        dependsOn(verifyThinJar)
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifactId = project.name
            }
        }
        repositories {
            val token = providers.environmentVariable("GITHUB_TOKEN")
            if (token.isPresent) {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/TavallStudios/Webstore")
                    credentials {
                        username = providers.environmentVariable("GITHUB_ACTOR").orNull
                        password = token.get()
                    }
                }
            }
        }
    }
}

project(":platform-internal-api") {
    dependencies {
        "api"("org.tavall:tavall-database-postgres:$tavallToolsVersion")
        "api"("org.tavall:tavall-registry:$tavallToolsVersion")
        "api"("org.tavall:tavall-logging:$tavallToolsVersion")
        "api"("org.springframework.boot:spring-boot")
        "api"("com.fasterxml.jackson.core:jackson-annotations")
        "api"("org.springframework:spring-context")
        "api"("org.springframework:spring-web")
        "api"("org.springframework:spring-tx")
        "api"("org.springframework.security:spring-security-config")
        "api"("org.springframework.security:spring-security-web")
        "api"("org.springframework.security:spring-security-oauth2-client")
        "api"("org.springframework.data:spring-data-jpa")
        "api"("jakarta.persistence:jakarta.persistence-api")
        "compileOnly"("jakarta.servlet:jakarta.servlet-api")
        "api"("jakarta.validation:jakarta.validation-api")
        "api"("org.hibernate.orm:hibernate-core")
        "api"("org.bouncycastle:bcpkix-jdk18on:1.83")
        "api"("io.fabric8:kubernetes-client:$fabric8Version")
        "testImplementation"("com.h2database:h2")
        "testImplementation"("org.springframework.boot:spring-boot-starter-data-jpa")
        "testImplementation"("org.springframework.boot:spring-boot-starter-security")
        "testImplementation"("org.springframework.boot:spring-boot-starter-oauth2-client")
        "testImplementation"("org.springframework.boot:spring-boot-starter-web")
        "testImplementation"("org.flywaydb:flyway-core")
        "testImplementation"("org.flywaydb:flyway-database-postgresql")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    }
}

fun Project.configureWebApplication(mainClassName: String, jarName: String) {
    apply(plugin = "application")
    extensions.configure<JavaApplication> {
        mainClass = mainClassName
    }
    tasks.named<Jar>("jar") {
        archiveFileName = jarName
        manifest {
            attributes["Main-Class"] = mainClassName
        }
    }
}

project(":platform-spring-webview") {
    configureWebApplication("org.tavall.platform.PlatformSpringWebviewApplication", "platform-spring-webview.jar")
    dependencies {
        "implementation"(project(":platform-internal-api"))
        "implementation"("org.tavall:tavall-concurrency:$tavallToolsVersion")
        "implementation"("org.tavall:tavall-logging:$tavallToolsVersion")
        "implementation"("org.springframework.boot:spring-boot-starter-web")
        "implementation"("org.springframework.boot:spring-boot-starter-validation")
        "implementation"("org.springframework.boot:spring-boot-starter-thymeleaf")
        "implementation"("org.springframework.boot:spring-boot-starter-actuator")
        "implementation"("org.springframework.boot:spring-boot-starter-security")
        "implementation"("org.springframework.boot:spring-boot-starter-oauth2-client")
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
        "implementation"("org.flywaydb:flyway-core")
        "implementation"("org.flywaydb:flyway-database-postgresql")
        "implementation"("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
        "runtimeOnly"("org.postgresql:postgresql")
        "testImplementation"("com.h2database:h2")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    }
}

project(":webstore-view") {
    configureWebApplication("org.tavall.webstore.WebstoreApplication", "webstore-view.jar")
    dependencies {
        "implementation"("org.tavall:tavall-concurrency:$tavallToolsVersion")
        "implementation"("org.tavall:tavall-logging:$tavallToolsVersion")
        "implementation"("org.springframework.boot:spring-boot-starter-web")
        "implementation"("org.springframework.boot:spring-boot-starter-thymeleaf")
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
        "implementation"("org.springframework.boot:spring-boot-starter-validation")
        "implementation"("org.springframework.boot:spring-boot-starter-actuator")
        "implementation"("org.flywaydb:flyway-core")
        "implementation"("org.flywaydb:flyway-database-postgresql")
        "runtimeOnly"("org.postgresql:postgresql")
        "runtimeOnly"("org.springframework.boot:spring-boot-devtools")
        "testImplementation"("com.h2database:h2")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    }
}

val stageDistribution = tasks.register<Sync>("stageDistribution") {
    val platform = project(":platform-spring-webview")
    val storefront = project(":webstore-view")
    dependsOn(platform.tasks.named("jar"), storefront.tasks.named("jar"))
    into(layout.projectDirectory.dir("distribution"))
    into("platform") {
        from(platform.tasks.named<Jar>("jar").flatMap { it.archiveFile }) {
            rename { "application.jar" }
        }
        into("libs") {
            from(platform.configurations.getByName("runtimeClasspath"))
        }
    }
    into("storefront") {
        from(storefront.tasks.named<Jar>("jar").flatMap { it.archiveFile }) {
            rename { "application.jar" }
        }
        into("libs") {
            from(storefront.configurations.getByName("runtimeClasspath"))
        }
    }
}

tasks.named("assemble") {
    dependsOn(stageDistribution)
}
