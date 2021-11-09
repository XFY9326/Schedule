plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = PROJECT_JAVA_VERSION
    targetCompatibility = PROJECT_JAVA_VERSION
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = PROJECT_JAVA_VERSION.toString()
    }
}

dependencies {
    api(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = Dependencies.kotlinx_coroutines)
}