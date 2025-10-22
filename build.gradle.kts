import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jreleaser.model.Active

plugins {
    kotlin("jvm") version "2.2.20"
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("maven-publish")
    id("signing")
    id("org.jreleaser") version "1.20.0" // Aktuelle Version prüfen
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String



base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
    withJavadocJar()
}


loom {
    splitEnvironmentSourceSets()

    mods {
        register("simplefabricscoreboard") {
            sourceSet("main")
        }
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version"),
            "kotlin_loader_version" to project.property("kotlin_loader_version")
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

signing {
}
configure<PublishingExtension> {
    publications {

        register<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set(project.description ?: project.name)
                url.set("https://github.com/hotkeyyy/simplefabricscoreboard")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/hotkeyyy/simplefabricscoreboard/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("Hotkeyyy")
                        name.set("Hotkeyyy")
                        email.set("hotkeyyyde@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/hotkeyyy/simplefabricscoreboard.git")
                    developerConnection.set("scm:git:ssh://github.com:hotkeyyy/simplefabricscoreboard.git")
                    url.set("https://github.com/hotkeyyy/simplefabricscoreboard")
                }
            }
        }
    }

    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}
// JReleaser Konfiguration
jreleaser {
    project {
        description = "A lightweight Kotlin module for Minecraft Fabric providing a simple and flexible API for managing scoreboards."
        authors = listOf("Hotkeyyy")
        license = "Apache-2.0"
        links{
            homepage = "https://github.com/hotkeyyy/simplefabricscoreboard"
        }
        inceptionYear = "2025"
        vendor = "Hotkeyyy"
    }
    signing {
        active = Active.ALWAYS
        setMode("FILE")
        armored = true
    }

    deploy {
        maven{
            mavenCentral {
                register("sonatype"){
                    active = Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository(layout.buildDirectory.dir("staging-deploy").get().asFile.path)

                }
            }
        }
    }
}