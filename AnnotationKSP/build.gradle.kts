plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = ProjectConfig.javaVersion
    targetCompatibility = ProjectConfig.javaVersion
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = ProjectConfig.javaVersion.toString()
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=com.google.devtools.ksp.KspExperimental",
            "-opt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview"
        )
    }
}

dependencies {
    implementation(project(path = ":Annotation"))
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.0-1.0.6")
}