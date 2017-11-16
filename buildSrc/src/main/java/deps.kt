import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin

/**
 * Dependencies definition
 */
object D {
    val kt_version = "1.1.60"
    val kt_stdlib = "stdlib-jre7"

    object Auto {
        val common = "com.google.auto:auto-common:0.8"
        val service = "com.google.auto.service:auto-service:1.0-rc3"
    }
    val android = "com.google.android:android:2.1.2"
    val ktpoet = "com.squareup:kotlinpoet:0.5.0"
    val gson = "com.google.code.gson:gson:2.8.0"

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
}

/** Kotlin dependency with default stdlib module */
val DependencyHandler.kt get() = kotlin(D.kt_stdlib)
