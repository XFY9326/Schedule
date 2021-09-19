plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = Dependencies.kotlinx_coroutines)
}