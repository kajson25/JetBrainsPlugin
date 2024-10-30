package myplugin.core

import com.intellij.openapi.project.Project
import java.io.File

object ConfigManager {
    private const val CONFIG_FILE_NAME = "entity-package-config.txt"

    fun getConfigFile(project: Project): File {
        val configDir = File(project.basePath, ".idea")
        if (!configDir.exists()) configDir.mkdirs()
        val configFile = File(configDir, CONFIG_FILE_NAME)
        if (!configFile.exists()) {
            configFile.writeText("# Enter package names (one per line) for entity generation\n")
        }
        return configFile
    }

    fun getConfiguredPackages(project: Project): List<String> {
        val configFile = getConfigFile(project)
        return if (configFile.exists()) {
            configFile
                .readLines()
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .flatMap { line -> line.split(",").map { it.trim() } }
                .filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
    }
}
