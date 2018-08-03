import D.ktlint
import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    kotlin("jvm") version D.kt_version
    kotlin("kapt") version D.kt_version apply false
    kotlin("android") version D.kt_version apply false
    id("com.jfrog.bintray") version D.bintray_version
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
version = "0.6.1"
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

    val ktlintCfg by configurations.creating // add ktlint configuration

    dependencies {
        ktlintCfg(D.ktlint)
    }

    tasks {
        val ktlint by creating(JavaExec::class) {
            group = "verification"
            description = "Check Kotlin code style."
            main = "com.github.shyiko.ktlint.Main"
            classpath = ktlintCfg
            args("--verbose", "--reporter=plain", "--reporter=checkstyle,output=$buildDir/reports/ktlint.xml", "src/**/*.kt")
        }

        createTask("ktlintFormat", JavaExec::class) {
            group = "formatting"
            description = "Fix Kotlin code style deviations."
            main = "com.github.shyiko.ktlint.Main"
            classpath = ktlintCfg
            args("-F", "src/**/*.kt")
        }

        afterEvaluate {
            tasks.findByPath("check")?.dependsOn(ktlint)
        }
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
