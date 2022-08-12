// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "7.2.2" apply false
    id("com.android.library") version "7.2.2" apply false
    kotlin("android") version "1.7.10" apply false
    kotlin("jvm") version "1.7.10" apply false
    kotlin("plugin.serialization") version "1.7.10" apply false
    id("com.google.devtools.ksp") version "1.7.10-1.0.6" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}