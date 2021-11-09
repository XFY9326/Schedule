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
    implementation(project(path = ":Annotation"))
    implementation(group = "com.squareup", name = "kotlinpoet", version = "1.10.2")
}