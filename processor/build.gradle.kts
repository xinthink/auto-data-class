import org.gradle.internal.jvm.Jvm

dependencies {
    val kt = kotlin(deps["kt"])
    compileOnly(kt)

    compile(project(":lib"))
    compile(deps["auto.common"])
//    compile(deps["auto.service"])
    compile(deps["kotlinpoet"])
    compile(deps["gson"])
    compileOnly(deps["android"])

    testRuntime(kt)
    testCompile(deps["junit"])
    testCompile(deps["google_testing.truth"])
    testCompile(deps["google_testing.compile"])
    testCompile(files(Jvm.current().toolsJar))
}
