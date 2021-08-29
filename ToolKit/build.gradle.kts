plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = Dependencies.kotlinx_coroutines)
}