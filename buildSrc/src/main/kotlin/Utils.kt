import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.plugin.use.PluginDependency

/**
 * Adds a plugin to the [PluginDependenciesSpecScope] extracting the plugin ID from the given
 * [PluginDependency] provider. Simplifies applying plugins when using buildSrc.
 */
fun PluginDependenciesSpecScope.id(notation: Provider<PluginDependency>) =
    id(notation.get().pluginId)