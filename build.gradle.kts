plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.moddev)
    id("maven-publish")
}

version = "0.1.4"
group = "coffee.cypher.hexbound"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

neoForge {
    version = libs.versions.neoforge.get()
    
    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
        }
        create("client") {
            client()
        }
        create("server") {
            server()
        }
    }
    
    mods {
        create("hexbound") {
            sourceSet(sourceSets.main.get())
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.blamejared.com")
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://mvn.devos.one/snapshots/")
    maven("https://mvn.devos.one/releases/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.geckolib)
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    compileOnly(libs.patchouli)
}

sourceSets {
    main {
        java {
            exclude("reference_1.20.1/**")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
