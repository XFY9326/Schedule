import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_11.toString()
}

dependencies {
    api(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = Dependencies.kotlinx_coroutines)
}