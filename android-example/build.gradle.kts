plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-android-extensions")
}

apply {
    // enable experimental mode, a workaround to https://github.com/gradle/kotlin-dsl/issues/644
    from("experimentalExtensions.gradle")
}

android {
    compileSdkVersion(D.Android.target_sdk)

    defaultConfig {
        applicationId = "com.fivemiles.dataclass.androidexample"
        versionCode = 1
        versionName = "1.0"
        targetSdkVersion(D.Android.target_sdk)
        minSdkVersion(D.Android.min_sdk)

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                includeCompileClasspath = false
            }
        }
    }
    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_7)
        setTargetCompatibility(JavaVersion.VERSION_1_7)
    }
    lintOptions {
        isAbortOnError = false
    }
}

dependencies {
    implementation(D.kotlin)
    implementation(D.Android.Support.appcompat)
    implementation(D.gson)
    implementation(D.javax_annotation)

    kapt(project(":processor"))
    implementation(project(":lib"))

    testImplementation(D.junit)
    androidTestImplementation(D.Android.Support.test_runner)
    androidTestImplementation(D.Espresso.core)

    kaptTest(project(":processor"))
    kaptAndroidTest(project(":processor"))
}
