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
    implementation(project(path = ":Annotation"))
    implementation("com.squareup:kotlinpoet:1.10.2")
}