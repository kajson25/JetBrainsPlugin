package myplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.IOException

class EntityCreationListener(private val project: Project) : PsiTreeChangeAdapter() {

    override fun childAdded(event: PsiTreeChangeEvent) {
        val psiElement = event.child
        println("Element: ${psiElement?.text} -> ${psiElement?.containingFile?.name}")
        if (psiElement is PsiClass) {
            println("Class creation detected.")

            ApplicationManager.getApplication().invokeLater {
                showConfirmationAndGenerateClasses(psiElement)
            }
        }
    }

    private fun showConfirmationAndGenerateClasses(psiClass: PsiClass) {
        val className = psiClass.name ?: return

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

    private fun generateSupportingClasses(psiClass: PsiClass) {
        val className = psiClass.name

        // Generate repository
        createClassInFolder("repository", "${className}Repository", psiClass)

        // Generate service
        createClassInFolder("service", "${className}Service", psiClass)

        // Generate controller
        createClassInFolder("controller", "${className}Controller", psiClass)
    }

    private fun createClassInFolder(folderName: String, newClassName: String, psiClass: PsiClass) {
        val projectBaseDir = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(project.basePath!!)
        println("ProjectBaseDir: $projectBaseDir")

        val srcDir = projectBaseDir?.findChild("src")?.findChild("main")?.findChild("kotlin")?.findChild(folderName)
        println("TARGET DIR: $srcDir")

        if (srcDir != null) {
            val newClassContent = """
            package $folderName;

            public class $newClassName {
                // TODO: Add logic for $newClassName
            }
        """.trimIndent()

            // Run file creation inside a write action
            ApplicationManager.getApplication().runWriteAction {
                try {
                    val newFile = srcDir.createChildData(this, "$newClassName.kt")
                    newFile.getOutputStream(this).writer().use { it.write(newClassContent) }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            println("Target directory $folderName not found")
        }
    }
}
