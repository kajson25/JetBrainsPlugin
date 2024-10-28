package myplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.util.removeSuffixIfPresent
import java.io.File
import java.io.IOException

class EntityCreationListener(private val project: Project) : PsiTreeChangeAdapter() {

    override fun childAdded(event: PsiTreeChangeEvent) {
        val psiElement = event.child

        val configuredPackages = getConfiguredPackages(project)

        when (psiElement) {
            is PsiJavaFile -> {
                val packageName = psiElement.packageName
                if (configuredPackages.any { packageName.matches(Regex(it)) }) {
                    ApplicationManager.getApplication().invokeLater {
                        psiElement.classes.forEach { psiClass ->
                            showConfirmationAndGenerateClasses(psiClass)
                        }
                    }
                }
            }
            is PsiFile -> {
                if (psiElement.name.endsWith(".kt") && configuredPackages.isNotEmpty()) {
                    val ktPackage = psiElement.parent?.name
                    if (configuredPackages.any { ktPackage?.matches(Regex(it)) == true }) {
                        ApplicationManager.getApplication().invokeLater {
                            showConfirmationAndGenerateClasses(psiElement)
                        }
                    }
                }
            }
        }
    }

    private fun showConfirmationAndGenerateClasses(psiClass: Any) {
        val className = when (psiClass) {
            is PsiClass -> psiClass.name
            is PsiFile -> psiClass.name.removeSuffixIfPresent(".kt")
            else -> return
        } ?: return

        // Show confirmation dialog to create main classes
        val response = Messages.showYesNoDialog(
            project,
            "You created the class $className. Do you want to generate the corresponding Repository, Service, and Controller classes?",
            "Generate Supporting Classes",
            Messages.getQuestionIcon()
        )

        if (response == Messages.YES) {
            // Ask if the user wants interfaces
            val interfaceResponse = Messages.showYesNoDialog(
                project,
                "Do you also want to create interfaces for the generated classes?",
                "Generate Interfaces",
                Messages.getQuestionIcon()
            )
            val createInterfaces = (interfaceResponse == Messages.YES)
            generateSupportingClasses(psiClass, createInterfaces)
            if (createInterfaces) {
                generateInterfaces(psiClass)
            }
        }
    }

    private fun generateSupportingClasses(psiClass: Any, createInterfaces: Boolean) {
        val className = when (psiClass) {
            is PsiClass -> psiClass.name
            is PsiFile -> psiClass.name.removeSuffixIfPresent(".kt")
            else -> return
        } ?: return

        // Adjust class names based on whether interfaces are created
        val repoClassName = if (createInterfaces) "${className}RepositoryImplementation" else "${className}Repository"
        val serviceClassName = if (createInterfaces) "${className}ServiceImplementation" else "${className}Service"
        val controllerClassName = if (createInterfaces) "${className}ControllerImplementation" else "${className}Controller"
        val mapperClassName = "${className}Mapper"
        val dtosRequestClassName = "${className}RequestDto"
        val dtosResponseClassName = "${className}ResponseDto"

        createClassInFolder("repository", repoClassName, psiClass, createInterfaces, "${className}Repository")
        createClassInFolder("service", serviceClassName, psiClass, createInterfaces, "${className}Service")
        createClassInFolder("controller", controllerClassName, psiClass, createInterfaces, "${className}Controller")
        createClassInFolder("mapper", mapperClassName, psiClass, createInterfaces, "")
        createClassInFolder("dtos", dtosRequestClassName, psiClass, createInterfaces, "")
        createClassInFolder("dtos", dtosResponseClassName, psiClass, createInterfaces, "")
    }

    private fun generateInterfaces(psiClass: Any) {
        val className = when (psiClass) {
            is PsiClass -> psiClass.name
            is PsiFile -> psiClass.name.removeSuffixIfPresent(".kt")
            else -> return
        } ?: return

        createInterfaceInFolder("repository", "${className}Repository", psiClass)
        createInterfaceInFolder("service", "${className}Service", psiClass)
        createInterfaceInFolder("controller", "${className}Controller", psiClass)
    }

    private fun createClassInFolder(folderName: String, className: String, psiClass: Any, implementsInterface: Boolean, interfaceName: String? = null) {
        createFileInFolder(folderName, className, psiClass, isInterface = false, implementsInterface = implementsInterface, interfaceName = interfaceName)
    }

    private fun createInterfaceInFolder(folderName: String, interfaceName: String, psiClass: Any) {
        createFileInFolder(folderName, interfaceName, psiClass, isInterface = true)
    }

    private fun createFileInFolder(folderName: String, fileName: String, psiClass: Any, isInterface: Boolean, implementsInterface: Boolean = false, interfaceName: String? = null) {
        val projectBaseDir = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(project.basePath!!)
        val languageFolder = if (psiClass is PsiClass) "java" else "kotlin"

        var srcDir: VirtualFile? = null
        ApplicationManager.getApplication().runWriteAction {
            srcDir = createOrGetDirectory(projectBaseDir, listOf("src", "main", languageFolder, folderName))
        }

        if (srcDir != null) {
            val (fileExtension, fileContent) = when (psiClass) {
                is PsiClass -> "java" to if (isInterface) {
                    """
                package $folderName;

                public interface $fileName {
                    // TODO: Define interface methods for $fileName
                }
                """.trimIndent()
                } else {
                    val implementsClause = if (implementsInterface && interfaceName != null) "implements $interfaceName" else ""
                    """
                package $folderName;

                public class $fileName $implementsClause {
                    // TODO: Add logic for $fileName
                }
                """.trimIndent()
                }
                is PsiFile -> "kt" to if (isInterface) {
                    """
                package $folderName

                interface $fileName {
                    // TODO: Define interface methods for $fileName
                }
                """.trimIndent()
                } else {
                    val implementsClause = if (implementsInterface && interfaceName != null) ": $interfaceName" else ""
                    """
                package $folderName

                class $fileName $implementsClause {
                    // TODO: Add logic for $fileName
                }
                """.trimIndent()
                }
                else -> return
            }

            ApplicationManager.getApplication().runWriteAction {
                try {
                    val newFile = srcDir!!.createChildData(this, "$fileName.$fileExtension")
                    newFile.getOutputStream(this).writer().use { it.write(fileContent) }
                    println("Created file: ${newFile.path}")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createOrGetDirectory(baseDir: VirtualFile?, pathParts: List<String>): VirtualFile? {
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
        configFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .flatMap { line -> line.split(",").map { it.trim() } }
            .filter { it.isNotEmpty() }
    } else {
        emptyList()
    }
}
