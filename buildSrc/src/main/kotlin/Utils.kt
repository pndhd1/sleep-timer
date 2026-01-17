import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.plugin.use.PluginDependency
import java.util.Properties

/**
 * Adds a plugin to the [PluginDependenciesSpecScope] extracting the plugin ID from the given
 * [PluginDependency] provider. Simplifies applying plugins when using buildSrc.
 */
fun PluginDependenciesSpecScope.id(notation: Provider<PluginDependency>) =
    id(notation.get().pluginId)

/**
 * Loads properties from a file in the root project directory.
 * Returns empty Properties if file doesn't exist.
 */
fun Project.loadProperties(fileName: String) = Properties().apply {
    rootProject.file(fileName).takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
