plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.bundles.lint)
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Lint-Registry-v2" to "com.superinit_lint_sample.CustomLintRegistry"))
    }
}