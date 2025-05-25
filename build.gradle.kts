import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.dokka") version "2.0.0"
    id("maven-publish")
}

group = "eu.wynq"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            moduleName.set("Inisaur")
            includes.from("README.md")

            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URI("https://github.com/JensvandeWiel/inisaur/blob/master/src/main/kotlin").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))

    // Add the dokka versioning plugin
    dokkaPlugin("org.jetbrains.dokka:versioning-plugin:2.0.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

val versionString = project.version.toString()

tasks.withType<DokkaTask>().configureEach {
    pluginsMapConfiguration.set(
        mapOf(
            "org.jetbrains.dokka.versioning.VersioningPlugin.version" to versionString,
            "org.jetbrains.dokka.versioning.VersioningPlugin.olderVersionsDir" to "${project.projectDir}/docs",
            "org.jetbrains.dokka.versioning.VersioningPlugin.renderVersionsNavigationOnAllPages" to "true"
        )
    )

    outputDirectory.set(buildDir.resolve("dokka"))
}

// Create a task to generate a version list in the docs directory
tasks.register("generateVersionList") {
    doLast {
        val versionsDir = file("${project.projectDir}/docs")
        val versions = versionsDir.listFiles { file -> file.isDirectory }
            ?.sortedByDescending { it.name }
            ?.map { it.name }
            ?: listOf()

        // Write the versions to a JSON file
        file("${project.projectDir}/docs/versions.json").writeText(
            """
            {
                "versions": ${versions.joinToString(prefix = "[\"", postfix = "\"]", separator = "\", \"")}
            }
            """.trimIndent()
        )
    }
}

tasks.register<Copy>("syncDokkaVersionedDocs") {
    dependsOn("dokkaHtml")

    val versionDir = file("docs/$versionString")
    from(layout.buildDirectory.dir("dokka"))
    into(versionDir)

    doFirst {
        versionDir.mkdirs()
    }

    finalizedBy("generateVersionList")
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml.get().outputDirectory)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        register<MavenPublication>("gpr") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/JensvandeWiel/inisaur")
            credentials {
                username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
