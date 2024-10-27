package myplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File

class SetEntityPackageAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val configFile = EntityCreationListener.getConfigFile(project)

        // Show input dialog
        val input = Messages.showInputDialog(
            project,
            "Enter package names (comma-separated) where entities should trigger class generation:",
            "Configure Entity Packages",
            Messages.getQuestionIcon()
        )

        input?.let {
            // Write input to config file
            configFile.writeText(it.lines().joinToString("\n"))
            Messages.showInfoMessage(project, "Configuration saved!", "Info")
        }
    }
}