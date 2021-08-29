plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(path = ":Annotation"))
    implementation(group = "com.squareup", name = "kotlinpoet", version = "1.9.0")
}