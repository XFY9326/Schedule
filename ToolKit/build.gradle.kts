plugins {
    id("java-library")
    id("kotlin")
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
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
}