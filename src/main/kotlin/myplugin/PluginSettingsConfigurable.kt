package myplugin

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList
import javax.swing.DefaultListModel
import javax.swing.JComponent
import java.io.File

class PluginSettingsConfigurable(private val project: Project) : Configurable {
    private val settingsState = PluginSettingsState.getInstance(project)
    private val settings = settingsState.settings

    private val listModel = DefaultListModel<String>().apply {
        settings.packagePatterns.forEach { addElement(it) }
    }
    private val packagePatternList = JBList(listModel)
    private val enableInterfacesCheckbox = JBCheckBox("Enable Interface Generation", settings.enableInterfaces)

    override fun createComponent(): JComponent {
        return panel {
            group("Package Patterns") {
                row {
                    scrollCell(packagePatternList)
                }
                row("Add Pattern:") {
                    val addPatternField = textField()
                        .columns(20)
                        .align(AlignX.FILL)
                        .component

                    button("Add") {
                        val newPattern = addPatternField.text.trim()
                        if (newPattern.isNotEmpty()) {
                            listModel.addElement(newPattern)
                            addPatternField.text = ""
                        }
                    }
                }
                row {
                    button("Remove Selected") {
                        packagePatternList.selectedValuesList.forEach { listModel.removeElement(it) }
                    }
                }
            }
            group("Settings") {
                row {
                    enableInterfacesCheckbox
                }
            }
        }
    }

    override fun isModified(): Boolean {
        return listModel.elements().toList() != settings.packagePatterns ||
                enableInterfacesCheckbox.isSelected != settings.enableInterfaces
    }

    override fun apply() {
        settings.packagePatterns = listModel.elements().toList().toMutableList()
        settings.enableInterfaces = enableInterfacesCheckbox.isSelected

        val configFile = File(project.basePath, ".idea/entity-package-config.txt")
        val packagePatternsAsString = settings.packagePatterns.joinToString(",")

        configFile.writeText(packagePatternsAsString)
    }

    override fun reset() {
        listModel.clear()
        settings.packagePatterns.forEach { listModel.addElement(it) }
        enableInterfacesCheckbox.isSelected = settings.enableInterfaces
    }

    override fun getDisplayName(): String = "My Plugin Settings"
}
