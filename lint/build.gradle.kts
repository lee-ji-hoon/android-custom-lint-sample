plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.android.lint)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

lint {
    htmlReport = true
    htmlOutput = file("lint-report.html")
    baseline = file("lint-baseline.xml")
//    textReport = true
//    absolutePaths = false
//    ignoreTestSources = true
}

dependencies {
    // For a description of the below dependencies, see the main project README
    compileOnly(libs.bundles.lint.api)
    testImplementation(libs.bundles.lint.tests)
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Lint-Registry-v2" to "com.superinit_lint_sample.CustomLintRegistry"))
    }
}