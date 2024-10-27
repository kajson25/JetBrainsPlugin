plugins {
    id("org.jetbrains.intellij") version "1.15.0"  // Ensure version is compatible with your IntelliJ version
    kotlin("jvm") version "1.8.10"  // Kotlin version for your plugin
}

intellij {
    version.set("2023.2")  // Set to the IntelliJ version you're targeting
    plugins.set(listOf("java", "Kotlin"))
//    plugins.set(listOf("java", "Spring", "Kotlin"))  // Add Spring and Kotlin support
}

tasks {
    patchPluginXml {
        version.set("1.0.1")
        sinceBuild.set("231")
        untilBuild.set("233.*")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}
