plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.3.1"
}

repositories {
    mavenCentral()
    maven("https://maven.hapily.me/snapshots")
}

dependencies {
    compileOnly("com.github.hapily04:skript-minestom:1.0.0-alpha.40")
    implementation("net.bytebuddy:byte-buddy:1.18.12")
    implementation("net.bytebuddy:byte-buddy-agent:1.18.12")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        configurations = project.configurations.runtimeClasspath.map { setOf(it) }

        relocate("net.bytebuddy", "com.novystxr.bytebuddy")
        relocate("net.bytebuddy.agent", "com.novystxr.bytebuddy.agent")
    }

    runServer {
        minecraftVersion("1.21.11")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
        dependsOn(shadowJar)
    }
}
