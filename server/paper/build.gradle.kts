plugins {
    id("su.plo.voice.plugin.relocate-kotlin")
}

group = "$group.paper"

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(libs.paper)
    compileOnly(libs.papi)

    compileOnly("su.plo.ustats:paper:${libs.versions.ustats.get()}")
    compileOnly("su.plo.slib:spigot:${libs.versions.slib.get()}")

    compileOnly(project(":server:common"))

    // shadow projects
    listOf(
        project(":api:common"),
        project(":api:server"),
        project(":api:server-common"),
        project(":server:common"),
        project(":server-common"),
        project(":common"),
        project(":protocol")
    ).forEach {
        shadow(it) {
            isTransitive = false
        }
    }

    // shadow external deps
    shadow(kotlin("stdlib-jdk8"))
    shadow(libs.kotlinx.coroutines)
    shadow(libs.kotlinx.coroutines.jdk8)
    shadow(libs.kotlinx.json)

    shadow(libs.guice) {
        exclude("com.google.guava")
    }

    shadow(libs.opus.concentus)
    shadow(libs.config)
    shadow(libs.crowdin) {
        isTransitive = false
    }
    shadow("su.plo.ustats:paper:${libs.versions.ustats.get()}")
    shadow("su.plo.slib:spigot:${libs.versions.slib.get()}") {
        isTransitive = false
    }
}

tasks {
    processResources {
        filesMatching(mutableListOf("plugin.yml", "paper-plugin.yml")) {
            expand(
                mutableMapOf(
                    "version" to version
                )
            )
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        archiveBaseName.set("PlasmoVoice-Paper")
        archiveAppendix.set("")
        archiveClassifier.set("")

        relocate("su.plo.crowdin", "su.plo.voice.libs.crowdin")
        relocate("su.plo.ustats", "su.plo.voice.libs.ustats")

        relocate("org.concentus", "su.plo.voice.libs.concentus")

        relocate("com.google.inject", "su.plo.voice.libs.google.inject")
        relocate("org.aopalliance", "su.plo.voice.libs.aopalliance")
        relocate("javax.inject", "su.plo.voice.libs.javax.inject")

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))
            exclude(dependency("org.jetbrains:annotations"))

            exclude("su/plo/opus/*")
            exclude("natives/opus/**/*")

            exclude("DebugProbesKt.bin")
            exclude("META-INF/**")
        }
    }

    build {
        dependsOn.add(shadowJar)

        doLast {
            shadowJar.get().archiveFile.get().asFile
                .copyTo(rootProject.buildDir.resolve("libs/${shadowJar.get().archiveFile.get().asFile.name}"), true)
        }
    }

    jar {
        enabled = false
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
