// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("compose_version", "1.6.8")
        set("kotlin_version", "1.9.10")
        set("hilt_version", "2.48")
        set("room_version", "2.6.0")
        set("retrofit_version", "2.9.0")
        set("okhttp_version", "4.12.0")
        set("exoplayer_version", "2.19.1")
        set("coil_version", "2.5.0")
        set("coroutines_version", "1.7.3")
        set("lifecycle_version", "2.7.0")
        set("navigation_version", "2.7.5")
        set("firebase_bom_version", "32.6.0")
    }
}

plugins {
    id("com.android.application") version "8.1.4" apply false
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.4" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}