plugins {
    id("org.jetbrains.intellij") version "1.15.0"
    kotlin("jvm") version "1.8.10"
}

intellij {
    version.set("2023.2") // Base version
    plugins.set(listOf("java", "Kotlin"))
}

tasks {
    patchPluginXml {
        version.set("1.1.0")
        sinceBuild.set("231") // Minimum compatible build version
        untilBuild.set("233.*")
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies") }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.8.10")
}
