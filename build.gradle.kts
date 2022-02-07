// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version ProjectConfig.androidGradlePluginVersion apply false
    id("com.android.library") version ProjectConfig.androidGradlePluginVersion apply false
    kotlin("android") version ProjectConfig.kotlinVersion apply false
    kotlin("jvm") version ProjectConfig.kotlinVersion apply false
    kotlin("plugin.serialization") version ProjectConfig.kotlinVersion apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}