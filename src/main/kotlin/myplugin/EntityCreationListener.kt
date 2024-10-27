package myplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.util.removeSuffixIfPresent
import java.io.IOException

class EntityCreationListener(private val project: Project) : PsiTreeChangeAdapter() {

    override fun childAdded(event: PsiTreeChangeEvent) {
        val psiElement = event.child
        println("Element: ${psiElement?.text} -> ${psiElement?.containingFile?.name}")

        when (psiElement) {
            is PsiJavaFile -> {
                println("Java file creation detected.")
                ApplicationManager.getApplication().invokeLater {
                    psiElement.classes.forEach { psiClass ->
                        showConfirmationAndGenerateClasses(psiClass)
                    }
                }
            }
            is PsiFile -> {
                if (psiElement.name.endsWith(".kt")) {
                    if(psiElement.name.contains("Repository") ||
                        psiElement.name.contains("Service") ||
                        psiElement.name.contains("Controller")) {
                        println("Detected wrong classes")
                        return
                    }
                    println("Kotlin file creation detected.")
                    ApplicationManager.getApplication().invokeLater {
                        showConfirmationAndGenerateClasses(psiElement)
                    }
                }
            }
        }
    }

private fun showConfirmationAndGenerateClasses(psiClass: Any) {
    val className = when (psiClass) {
        is PsiClass -> psiClass.name
        is PsiFile -> psiClass.name
        else -> return
    } ?: return

        // Show confirmation dialog
        val response = Messages.showYesNoDialog(
            project,
            "You created the class $className. Do you want to generate the corresponding Repository, Service, and Controller classes?",
            "Generate Supporting Classes",
            Messages.getQuestionIcon()
        )

        if (response == Messages.YES) {
            generateSupportingClasses(psiClass)
        }
    }

    private fun generateSupportingClasses(psiClass: Any) {
        val className = when (psiClass) {
            is PsiClass -> psiClass.name
            is PsiFile -> psiClass.name.removeSuffixIfPresent(".kt")
            else -> return
        } ?: return

        createClassInFolder("repository", "${className}Repository", psiClass)

        createClassInFolder("service", "${className}Service", psiClass)

        createClassInFolder("controller", "${className}Controller", psiClass)
    }

    private fun createClassInFolder(folderName: String, newClassName: String, psiClass: Any) {
        val projectBaseDir = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(project.basePath!!)
        println("ProjectBaseDir: $projectBaseDir")

         val srcDir: VirtualFile? = when (psiClass) {
            is PsiClass -> projectBaseDir?.findChild("src")?.findChild("main")?.findChild("java")?.findChild(folderName)
            is PsiFile -> projectBaseDir?.findChild("src")?.findChild("main")?.findChild("kotlin")?.findChild(folderName)
            else -> null
        }
        println("Target directory: $srcDir")

        // Prepare the file extension and content template based on the file type
        val (fileExtension, newClassContent) = when (psiClass) {
            is PsiClass -> "java" to """
            package $folderName;

            public class $newClassName {
                // TODO: Add logic for $newClassName
            }
        """.trimIndent()
            is PsiFile -> "kt" to """
            package $folderName

            class $newClassName {
                // TODO: Add logic for $newClassName
            }
        """.trimIndent()
            else -> return
        }

        if (srcDir != null) {
            ApplicationManager.getApplication().runWriteAction {
                try {
                    // Create the new file with appropriate extension
                    val newFile = srcDir.createChildData(this, "$newClassName.$fileExtension")
                    newFile.getOutputStream(this).writer().use { it.write(newClassContent) }
                    println("Created file: ${newFile.path}")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            println("Target directory $folderName not found")
        }
    }

}
