import org.gradle.api.Project

operator fun Project.contains(propertyName: String): Boolean = hasProperty(propertyName)
