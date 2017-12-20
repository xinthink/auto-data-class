import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    kotlin("jvm") version D.kt_version
    kotlin("kapt") version D.kt_version apply false
    kotlin("android") version D.kt_version apply false
    id("com.jfrog.bintray") version "1.7.3"
}

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${D.Android.plugin_version}")
    }
}

group = "com.fivemiles.auto"
version = "0.5.3"
loadProperties("local.properties", ext)  // load local.properties into ext

subprojects {
    if (project.name != "android-example") {
        apply {
            plugin("kotlin")
        }

        withConvention(JavaPluginConvention::class) {
            sourceCompatibility = JavaVersion.VERSION_1_7
            targetCompatibility = JavaVersion.VERSION_1_7
        }
    }

    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}

configure(subprojects.filterNot { "example" in it.name }) {
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
