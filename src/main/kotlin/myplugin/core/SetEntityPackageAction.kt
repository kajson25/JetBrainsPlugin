package myplugin.core

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class SetEntityPackageAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val configFile = ConfigManager.getConfigFile(project)

        val input =
            Messages.showInputDialog(
                project,
                "Enter package names (comma-separated) where entities should trigger class generation:",
                "Configure Entity Packages",
                Messages.getQuestionIcon(),
            )

        input?.let {
            configFile.writeText(it.lines().joinToString("\n"))
            Messages.showInfoMessage(project, "Configuration saved!", "Info")
        }
    }
}
