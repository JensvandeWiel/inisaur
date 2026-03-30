plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    id("org.jetbrains.dokka") version "2.2.0"
    id("maven-publish")
}

group = "eu.wynq"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))

    dokkaPlugin("org.jetbrains.dokka:versioning-plugin:2.2.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

val versionString = project.version.toString()

dokka {
    moduleName.set("Inisaur")

    dokkaSourceSets.main {
        includes.from("README.md")

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/JensvandeWiel/inisaur/blob/master/src/main/kotlin")
            remoteLineSuffix.set("#L")
        }
    }

    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka"))
    }

    pluginsConfiguration {
        versioning {
            version.set(versionString)
            olderVersionsDir.set(project.projectDir.resolve("docs"))
            renderVersionsNavigationOnAllPages.set(true)
        }
    }
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
    dependsOn("dokkaGeneratePublicationHtml")

    val versionDir = file("docs/$versionString")
    from(layout.buildDirectory.dir("dokka"))
    into(versionDir)

    doFirst {
        versionDir.mkdirs()
    }

    finalizedBy("generateVersionList")
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn("dokkaGeneratePublicationHtml")
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("dokka"))
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
