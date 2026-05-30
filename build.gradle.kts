// Root build.gradle.kts

import org.gradle.api.JavaVersion

plugins {
    alias(libs.plugins.android.application) apply false
    id("com.android.library") version libs.versions.agp.get() apply false
    alias(libs.plugins.kotlin.android) apply false
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin.get() apply false
}

// Build configuration constants
object BuildConfig {
    const val targetSdk = 34
    const val compileSdk = 34
    const val minSdk = 26
    const val versionCode = 1
    const val versionName = "1.0.0"
    val javaVersion = JavaVersion.VERSION_17
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
