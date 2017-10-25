import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.kotlin
import java.io.File
import java.io.FileInputStream
import java.util.*

operator fun Project.contains(propertyName: String): Boolean = hasProperty(propertyName)

fun Project.loadProperties(path: String, extra: ExtraPropertiesExtension) {
    val file: File = file(path)
    if (!file.exists()) return
    Properties().apply {
        load(FileInputStream(file))
        forEach { (k, v) ->
            extra["$k"] = v
        }
    }
}

fun DependencyHandler.kotlin(module: CharSequence) = kotlin(module.toString())

fun DependencyHandler.kotlin(module: DependencyItem)
        = kotlin(module as? CharSequence
        ?: throw IllegalArgumentException("Unexpected module type for $module"))
