import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin

/**
 * Dependencies definition
 */
object D {
    val kt_version = "1.2.10"
    val kt_stdlib = "stdlib-jre7"
    val kotlin = "org.jetbrains.kotlin:kotlin-$kt_stdlib:$kt_version"

    object Android {
        val plugin_version = "3.0.1"
        val target_sdk = 26
        val min_sdk = 21
        val build_tools_version = "26.0.2"
        val support_libs_version = "26.1.0"

        val stub = "com.google.android:android:2.1.2"

        object Support {
            val appcompat = "com.android.support:appcompat-v7:$support_libs_version"
            val test_runner = "com.android.support.test:runner:1.0.1"
        }
    }

    object Auto {
        val common = "com.google.auto:auto-common:0.8"
        val service = "com.google.auto.service:auto-service:1.0-rc3"
    }
    val ktpoet = "com.squareup:kotlinpoet:0.5.0"
    val gson = "com.google.code.gson:gson:2.8.2"
    val javax_annotation = "org.glassfish:javax.annotation:10.0-b28"

    // for testing
    val junit = "junit:junit:4.12"
    object GoogleTest {
        val truth = "com.google.truth:truth:0.27"
        val compile = "com.google.testing.compile:compile-testing:0.9"
    }
    object Mockito {
        val core = "org.mockito:mockito-core:2.10.0"
        val inline = "org.mockito:mockito-inline:2.10.0"
    }
    object Espresso {
        val core = "com.android.support.test.espresso:espresso-core:3.0.1"
    }
}

/** Kotlin dependency with default stdlib module */
val DependencyHandler.kt get() = kotlin(D.kt_stdlib)
