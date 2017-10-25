dependencies {
    val kt = kotlin("stdlib-jre7")
    compileOnly(kt)

    compileOnly(deps["android"])
    compileOnly(deps["gson"])

    testRuntime(kt)
    testCompile(deps["junit"])
}
