import org.gradle.internal.jvm.Jvm

dependencies {
    val kt = kotlin("stdlib-jre7")
    compileOnly(kt)

    compile(project(":lib"))
    compile(deps["auto"]["common"])
//    compile(deps["auto"]["service"])
    compile(deps["kotlinpoet"])
    compile(deps["gson"])
    compileOnly(deps["android"])

    testRuntime(kt)
    testCompile(deps["junit"])
    testCompile(deps["truth"])
    testCompile(deps["compile_testing"])
    testCompile(files(Jvm.current().toolsJar))
}
