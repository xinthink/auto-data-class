import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    kotlin("kapt")
}

configure<SourceSetContainer> {
    all {
        withConvention(KotlinSourceSet::class) {
            kotlin.srcDir("$buildDir/generated/source/kapt/$name")
        }
    }
}

dependencies {
    compileOnly(kt)

    compile(project(":lib"))
    compile(D.gson)
    compile(D.Android.stub)
    kapt(project(":processor"))
    compileOnly(project(":processor"))

    kaptTest(project(":processor"))
    testCompileOnly(project(":processor"))

    testRuntime(kt)
    testCompile(D.junit)
    testCompile(D.Mockito.inline)
}
