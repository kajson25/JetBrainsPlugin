package myplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.*
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiDirectory

class EntityCreationListener(private val project: Project) : PsiTreeChangeAdapter() {
    override fun childAdded(event: PsiTreeChangeEvent) {
        val psiElement = event.child
        val psiClass = psiElement as? PsiClass

        if (psiClass != null && isEntityClass(psiClass)) {
            // Generate repository, service, and controller
            generateSupportingClasses(psiClass)
        }
    }

    private fun isEntityClass(psiClass: PsiClass): Boolean {
        // Check if the class is inside a domain/model folder and has @Entity annotation
        return psiClass.hasAnnotation("javax.persistence.Entity") || psiClass.hasAnnotation("org.springframework.data.jpa.domain.AbstractPersistable")
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
        val targetDir = projectBaseDir?.findChild(folderName)

        if (targetDir != null) {
            val newClassContent = """
                package ${targetDir.name};

                public class $newClassName {
                    // TODO: Add logic for $newClassName
                }
            """.trimIndent()

            // Create the new file
            targetDir.createChildData(this, "$newClassName.kt").getOutputStream(this).writer().use {
                it.write(newClassContent)
            }
        }
    }

    fun generatePsiClass(directory: PsiDirectory, className: String, classContent: String) {
        val factory = PsiFileFactory.getInstance(directory.project)
        val psiFile = factory.createFileFromText("$className.kt", directory.language, classContent)
        directory.add(psiFile)
    }
}
