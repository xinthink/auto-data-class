import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    val kotlinVersion = "1.1.51"

    kotlin("jvm") version kotlinVersion apply false
    kotlin("kapt") version kotlinVersion apply false
    id("com.jfrog.bintray") version "1.7.3"
}

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
}

group = "com.fivemiles.auto"
version = "0.3.0"
loadProperties("local.properties", ext)  // load properties from local.properties

subprojects {
    extra.deps {
        "kt"("stdlib-jre7")
        "auto" {
            "common"("com.google.auto:auto-common:0.8")
            "service"("com.google.auto.service:auto-service:1.0-rc3")
        }
        "android"("com.google.android:android:2.1.2")
        "kotlinpoet"("com.squareup:kotlinpoet:0.5.0")
        "gson"("com.google.code.gson:gson:2.8.0")

        // for testing
        "junit"("junit:junit:4.12")
        "google_testing" {
            "truth"("com.google.truth:truth:0.27")
            "compile"("com.google.testing.compile:compile-testing:0.9")
        }
        "mockito" {
            "core"("org.mockito:mockito-core:2.10.0")
            "inline"("org.mockito:mockito-inline:2.10.0")
        }
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
