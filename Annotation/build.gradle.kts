plugins {
    id("java-library")
    kotlin("jvm")
}

java {
    sourceCompatibility = ProjectConfig.javaVersion
    targetCompatibility = ProjectConfig.javaVersion
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = ProjectConfig.javaVersion.toString()
    }
}

dependencies {
    api("androidx.annotation:annotation:1.3.0")
}