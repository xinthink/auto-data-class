import org.gradle.internal.jvm.Jvm

dependencies {
    compileOnly(kt)

    compile(project(":lib"))
    compile(D.Auto.common)
//    compile(D.Auto.service)
    compile(D.ktpoet)
    compile(D.gson)
    compileOnly(D.Android.stub)

    testRuntime(kt)
    testCompile(D.junit)
    testCompile(D.GoogleTest.truth)
    testCompile(D.GoogleTest.compile)
    testCompile(files(Jvm.current().toolsJar))
}
