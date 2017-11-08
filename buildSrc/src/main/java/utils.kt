import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
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
