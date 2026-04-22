plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    kotlin("plugin.serialization") version "1.9.0"
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        // Google Services 플러그인 추가
        classpath("com.google.gms:google-services:4.4.4")
    }
}