// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("build-plugin")
}

buildscript {
    val kotlinVersion = "1.5.30"

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(group = "com.android.tools.build", name = "gradle", version = "7.0.1")
        classpath(kotlin(module = "gradle-plugin", version = kotlinVersion))
        classpath(kotlin(module = "serialization", version = kotlinVersion))

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}