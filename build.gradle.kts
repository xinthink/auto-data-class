import java.io.FileInputStream
import java.util.Properties

import com.jfrog.bintray.gradle.BintrayExtension
import DependencyGroup.Companion.group


plugins {
    val kotlinVersion = "1.1.51"

    kotlin("jvm") version kotlinVersion apply false
    kotlin("kapt") version kotlinVersion apply false
    id("com.jfrog.bintray") version "1.7.3" apply false
}

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
}

group = "com.fivemiles.auto"
version = "0.2.0"

// load local.properties
val localPropsFile: File = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    Properties().apply {
        load(FileInputStream(localPropsFile))
        forEach { (k, v) ->
            rootProject.ext["$k"] = v
        }
    }
}

subprojects {
    extra.deps {
        set(
            "auto" to group(
                "common" to "com.google.auto:auto-common:0.8",
                "service" to "com.google.auto.service:auto-service:1.0-rc3"
            ),
            "android" to "com.google.android:android:2.1.2",
            "kotlinpoet" to "com.squareup:kotlinpoet:0.5.0",
            "gson" to "com.google.code.gson:gson:2.8.0",

            // for testing
            "junit" to "junit:junit:4.12",
            "truth" to "com.google.truth:truth:0.27",
            "compile_testing" to "com.google.testing.compile:compile-testing:0.9",
            "mockito" to group(
                "core" to "org.mockito:mockito-core:2.10.0",
                "inline" to "org.mockito:mockito-inline:2.10.0"
            )
        )
    }

    apply {
        plugin("kotlin")
    }

    withConvention(JavaPluginConvention::class) {
        sourceCompatibility = JavaVersion.VERSION_1_7
        targetCompatibility = JavaVersion.VERSION_1_7
    }

    repositories {
        jcenter()
        mavenCentral()
    }
}

configure(subprojects.filterNot { it.name == "example" }) {
    apply {
        plugin("maven-publish")
        plugin("com.jfrog.bintray")
    }

    ext["artifactId"] = "auto-data-class-${project.name}"

    withConvention(JavaPluginConvention::class) {
        val sourceJar by tasks.creating(Jar::class) {
            classifier = "sources"
            from(sourceSets["main"].allSource)
        }

        extensions.configure(PublishingExtension::class.java) {
            (publications) {
                "mavenJava"(MavenPublication::class) {
                    from(components["java"])
                    artifact(sourceJar)
                    groupId = "${rootProject.group}"
                    artifactId = "${project.ext["artifactId"]}"
                    version = "${rootProject.version}"
                }
            }
        }

        if ("BINTRAY_USER" !in rootProject) return@withConvention

        extensions.configure(BintrayExtension::class.java) {
            user = "${rootProject.ext["BINTRAY_USER"]}"
            key = "${rootProject.ext["BINTRAY_KEY"]}"
            setPublications("mavenJava")

            isDryRun = false
            isPublish = true

            pkg(closureOf<BintrayExtension.PackageConfig> {
                repo = "maven"
                name = "${project.ext["artifactId"]}"
                setLicenses("Apache-2.0")
                vcsUrl = "https://github.com/xinthink/auto-data-class.git"
                version(closureOf<BintrayExtension.VersionConfig> {
                    name = "${rootProject.version}"
                    vcsTag = "v${rootProject.version}"
                })
            })
        }
    }
}
