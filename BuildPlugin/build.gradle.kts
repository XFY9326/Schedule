plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

gradlePlugin {
    plugins.register("build-plugin") {
        id = "build-plugin"
        implementationClass = "tool.xfy9326.build.plugin.BuildPlugin"
    }
}