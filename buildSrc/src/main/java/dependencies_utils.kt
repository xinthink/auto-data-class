import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.extra

interface DependencyItem {
    operator fun get(key: String): Any
}

data class DependencyNotation(private val notation: String) : CharSequence by notation, DependencyItem {
    override operator fun get(key: String): String = notation
    override fun toString() = notation
}

class DependencyGroup : DependencyItem {
    private val dependencies = mutableMapOf<String, DependencyItem>()

    override operator fun get(key: String): DependencyItem
            = dependencies[key] ?: throw IllegalArgumentException("dependency key `$key` not found")

    operator fun set(key: String, item: DependencyItem) = dependencies.set(key, item)

    operator fun set(key: String, notation: String) = dependencies.set(key, DependencyNotation(notation))

    operator fun contains(key: String): Boolean = key in dependencies

    operator fun invoke(config: DependencyGroup.() -> Unit): DependencyGroup {
        this.config()
        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun set(dependencies: Map<String, Any>): DependencyGroup {
        dependencies.forEach {
            val (key, item) = it
            when (item) {
                is String -> set(key, item)
                is DependencyItem -> set(key, item)
                is Map<*, *> -> set(key, group(item as Map<String, Any>))
                else -> throw IllegalArgumentException("Unsupported dependency item type of `$item`")
            }
        }
        return this
    }

    fun set(vararg dependencies: Pair<String, Any>): DependencyGroup
            = set(mapOf(*dependencies))

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun group(dependencies: Map<String, Any>): DependencyGroup {
            val inst = DependencyGroup()
            dependencies.forEach {
                val (key, item) = it
                when (item) {
                    is String -> inst[key] = item
                    is DependencyItem -> inst[key] = item
                    is Map<*, *> -> inst[key] = group(item as Map<String, Any>)
                    else -> throw IllegalArgumentException("Unsupported dependency item type of `$item`")
                }
            }
            return inst
        }

        fun group(vararg dependencies: Pair<String, Any>): DependencyGroup
                = group(mapOf(*dependencies))
    }
}

val ExtraPropertiesExtension.deps: DependencyGroup
    get() {
        if (has("deps")) return get("deps") as DependencyGroup
        val group = DependencyGroup()
        set("deps", group)
        return group
    }

val Project.deps: DependencyGroup get() = extra.deps
