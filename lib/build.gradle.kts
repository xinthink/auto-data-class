dependencies {
    val kt = kotlin(deps["kt"])
    compileOnly(kt)

    compileOnly(deps["android"])
    compileOnly(deps["gson"])

    testRuntime(kt)
    testCompile(deps["junit"])
}
