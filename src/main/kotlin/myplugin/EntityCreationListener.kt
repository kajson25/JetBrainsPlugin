@file:Suppress("ktlint:standard:no-wildcard-imports")

package myplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.*
import org.jetbrains.kotlin.util.removeSuffixIfPresent
import java.io.File
import java.io.IOException

class EntityCreationListener(
    private val project: Project,
) : PsiTreeChangeAdapter() {
    override fun childAdded(event: PsiTreeChangeEvent) {
        val psiElement = event.child

        val configuredPackages = getConfiguredPackages(project)
        if (configuredPackages.isEmpty()) {
            return
        }

        when (psiElement) {
            is PsiJavaFile -> {
                val packageName = psiElement.parent?.name
                println("Package $packageName")
                if (configuredPackages.any { it.contains(packageName!!) }) {
                    ApplicationManager.getApplication().invokeLater {
                        showConfirmationAndGenerateClasses(psiElement)
                    }
                }
            }
            is PsiFile -> {
                if (psiElement.name.endsWith(".kt")) {
                    val ktPackage = psiElement.parent?.name
                    println(ktPackage)
                    println(configuredPackages.any { it.contains(ktPackage!!) })
                    if (configuredPackages.any { it.contains(ktPackage!!) }) {
                        ApplicationManager.getApplication().invokeLater {
                            showConfirmationAndGenerateClasses(psiElement)
                        }
                    }
                }
            }
        }
    }

    private fun showConfirmationAndGenerateClasses(psiFile: Any) {
        val className =
            when (psiFile) {
                is PsiJavaFile -> psiFile.name
                is PsiFile -> psiFile.name.removeSuffixIfPresent(".kt")
                else -> return
            }

        println("Usao u funkciju: $psiFile")

        // Show confirmation dialog to create main classes
        val response =
            Messages.showYesNoDialog(
                project,
                "You created the class $className. Do you want to generate the corresponding Repository, Service, and Controller classes?",
                "Generate Supporting Classes",
                Messages.getQuestionIcon(),
            )

        if (response == Messages.YES) {
            // Ask if the user wants interfaces
            val interfaceResponse =
                Messages.showYesNoDialog(
                    project,
                    "Do you also want to create interfaces for the generated classes?",
                    "Generate Interfaces",
                    Messages.getQuestionIcon(),
                )
            val createInterfaces = (interfaceResponse == Messages.YES)
            generateSupportingClasses(psiFile, createInterfaces)
            if (createInterfaces) {
                generateInterfaces(psiFile)
            }
        }
    }

    private fun generateSupportingClasses(
        psiFile: Any,
        createInterfaces: Boolean,
    ) {
        val className =
            when (psiFile) {
                is PsiJavaFile -> psiFile.name.removeSuffixIfPresent(".java")
                is PsiFile -> psiFile.name.removeSuffixIfPresent(".kt")
                else -> return
            }

        // Adjust class names based on whether interfaces are created
        val repoClassName = if (createInterfaces) "${className}RepositoryImplementation" else "${className}Repository"
        val serviceClassName = if (createInterfaces) "${className}ServiceImplementation" else "${className}Service"
        val controllerClassName = if (createInterfaces) "${className}ControllerImplementation" else "${className}Controller"
        val mapperClassName = "${className}Mapper"
        val dtosRequestClassName = "${className}RequestDto"
        val dtosResponseClassName = "${className}ResponseDto"

        createClassInFolder("repository", repoClassName, psiFile, createInterfaces, "${className}Repository")
        createClassInFolder("service", serviceClassName, psiFile, createInterfaces, "${className}Service")
        createClassInFolder("controller", controllerClassName, psiFile, createInterfaces, "${className}Controller")
        createClassInFolder("mapper", mapperClassName, psiFile, false, "")
        createClassInFolder("dtos", dtosRequestClassName, psiFile, false, "")
        createClassInFolder("dtos", dtosResponseClassName, psiFile, false, "")
    }

    private fun generateInterfaces(psiFile: Any) {
        val className =
            when (psiFile) {
                is PsiJavaFile -> psiFile.name.removeSuffixIfPresent(".java")
                is PsiFile -> psiFile.name.removeSuffixIfPresent(".kt")
                else -> return
            }

        createInterfaceInFolder("repository", "${className}Repository", psiFile)
        createInterfaceInFolder("service", "${className}Service", psiFile)
        createInterfaceInFolder("controller", "${className}Controller", psiFile)
    }

    private fun createClassInFolder(
        folderName: String,
        className: String,
        psiFile: Any,
        implementsInterface: Boolean,
        interfaceName: String? = null,
    ) {
        createFileInFolder(
            folderName,
            className,
            psiFile,
            isInterface = false,
            implementsInterface = implementsInterface,
            interfaceName = interfaceName,
        )
    }

    private fun createInterfaceInFolder(
        folderName: String,
        interfaceName: String,
        psiFile: Any,
    ) {
        createFileInFolder(folderName, interfaceName, psiFile, isInterface = true)
    }

    private fun createFileInFolder(
        folderName: String,
        fileName: String,
        psiFile: Any,
        isInterface: Boolean,
        implementsInterface: Boolean = false,
        interfaceName: String? = null,
    ) {
        val directoryPath: String?
        val entityPackageName: String?

        when (psiFile) {
            is PsiJavaFile -> {
                directoryPath = psiFile.virtualFile.parent.path
                entityPackageName = psiFile.packageName
            }
            is PsiFile -> {
                directoryPath = psiFile.virtualFile.parent.path
                entityPackageName = psiFile.parent?.name
            }
            else -> return
        }

        val baseDir =
            VirtualFileManager
                .getInstance()
                .getFileSystem("file")
                .findFileByPath(directoryPath)
                ?.parent ?: return

        val fullPathParts = listOf(folderName)
        var targetDir: VirtualFile? = null
        ApplicationManager.getApplication().runWriteAction {
            targetDir = createOrGetDirectory(baseDir, fullPathParts)
        }
        var entityPackage: String? = null
        getConfiguredPackages(project).forEach {
            if (it.contains(entityPackageName!!)) {
                entityPackage = it.substringBeforeLast(".")
            }
        }

        if (targetDir != null) {
            val (fileExtension, fileContent) =
                when (psiFile) {
                    is PsiJavaFile ->
                        "java" to
                            if (isInterface) {
                                """
                                package $entityPackage.$folderName;
                
                                public interface $fileName {
                                    // TODO: Define interface methods for $fileName
                                }
                                """.trimIndent()
                            } else {
                                val implementsClause = if (implementsInterface && interfaceName != null) "implements $interfaceName" else ""
                                """
                                package $entityPackage.$folderName;
                
                                public class $fileName $implementsClause {
                                    // TODO: Add logic for $fileName
                                }
                                """.trimIndent()
                            }
                    else ->
                        "kt" to
                            if (isInterface) {
                                """
                                package $entityPackage.$folderName
    
                                interface $fileName {
                                    // TODO: Define interface methods for $fileName
                                }
                                """.trimIndent()
                            } else {
                                val implementsClause = if (implementsInterface && interfaceName != null) ": $interfaceName" else ""
                                """
                                package $entityPackage.$folderName
                
                                class $fileName $implementsClause {
                                    // TODO: Add logic for $fileName
                                }
                                """.trimIndent()
                            }
                }

            // Create the new file in the specified directory
            ApplicationManager.getApplication().runWriteAction {
                try {
                    val newFile = targetDir!!.createChildData(this, "$fileName.$fileExtension")
                    newFile.getOutputStream(this).writer().use { it.write(fileContent) }
                    println("Created file: ${newFile.path}")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createOrGetDirectory(
        baseDir: VirtualFile?,
        pathParts: List<String>,
    ): VirtualFile? {
        var currentDir = baseDir
        for (part in pathParts) {
            currentDir = currentDir?.findChild(part) ?: currentDir?.createChildDirectory(this, part) // recursively making
        }
        return currentDir
    }
}

const val CONFIG_FILE_NAME = "entity-package-config.txt"

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
