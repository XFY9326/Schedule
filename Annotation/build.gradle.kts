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
    api(group = "androidx.annotation", name = "annotation", version = Dependencies.androidx_annotation)
}