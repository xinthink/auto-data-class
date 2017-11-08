import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    kotlin("kapt")
}

java.sourceSets.forEach {
    it.withConvention(KotlinSourceSet::class) {
        kotlin.srcDir("$buildDir/generated/source/kapt/${it.name}")
    }
}

dependencies {
    compileOnly(kt)

    compile(project(":lib"))
    compile(D.gson)
    compile(D.android)
    kapt(project(":processor"))
    compileOnly(project(":processor"))

    kaptTest(project(":processor"))
    testCompileOnly(project(":processor"))

    testRuntime(kt)
    testCompile(D.junit)
    testCompile(D.Mockito.inline)
}
