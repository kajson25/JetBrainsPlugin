package myplugin

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

// Define settings data structure
data class PluginSettings(
    var packagePatterns: MutableList<String> = mutableListOf("domain", "entities"),
    var enableInterfaces: Boolean = true
)

@Service(Service.Level.PROJECT)
@State(name = "MyPluginSettings", storages = [Storage("myplugin-settings.xml")])
class PluginSettingsState : PersistentStateComponent<PluginSettingsState> {
    var settings = PluginSettings()

    override fun getState(): PluginSettingsState = this

    override fun loadState(state: PluginSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): PluginSettingsState =
            project.getService(PluginSettingsState::class.java)
    }
}
