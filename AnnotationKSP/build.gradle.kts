plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = ProjectConfig.javaVersion
    targetCompatibility = ProjectConfig.javaVersion
}

tasks.compileKotlin {
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
    implementation("com.squareup:kotlinpoet-ksp:1.10.2")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.21-1.0.5")
}