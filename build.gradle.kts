// Root build.gradle.kts

import org.gradle.api.JavaVersion

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

// Build configuration constants
object BuildConfig {
    const val TARGET_SDK = 34
    const val COMPILE_SDK = 34
    const val MIN_SDK = 26
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0.0"
    val JAVA_VERSION = JavaVersion.VERSION_17
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
