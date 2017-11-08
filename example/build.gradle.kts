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
    val kt = kotlin(deps["kt"])
    compileOnly(kt)

    compile(project(":lib"))
    compile(deps["gson"])
    compile(deps["android"])
    kapt(project(":processor"))
    compileOnly(project(":processor"))

    kaptTest(project(":processor"))
    testCompileOnly(project(":processor"))

    testRuntime(kt)
    testCompile(deps["junit"])
    testCompile(deps["mockito.inline"])
}
