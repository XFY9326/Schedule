@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.AppExtension

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

object ProjectConfig {
    const val name = "PureSchedule"
    const val compileSdk = 33
    const val targetSdk = 33
    const val minSdk = 23
    const val applicationId = "tool.xfy9326.schedule"
    const val versionCode = 38
    const val versionName = "1.4.9"
}

android {
    namespace = ProjectConfig.applicationId
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        applicationId = ProjectConfig.applicationId
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName

        resourceConfigurations += "zh"

        buildConfigField("String", "BASE_APPLICATION_ID", "\"${ProjectConfig.applicationId}\"")
        buildConfigField("String", "PROJECT_NAME", "\"${ProjectConfig.name}\"")
        buildConfigField("boolean", "IS_BETA", "false")
        manifestPlaceholders["ApplicationId"] = ProjectConfig.applicationId
        manifestPlaceholders["BaseApplicationId"] = ProjectConfig.applicationId

        packaging {
            resources.excludes.addAll(
                arrayOf(
                    "META-INF/*.version",
                    "META-INF/CHANGES",
                    "META-INF/README.md",
                    "okhttp3/internal/publicsuffix/NOTICE",
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.txt",
                    "META-INF/license.txt",
                    "META-INF/NOTICE",
                    "META-INF/NOTICE.txt",
                    "META-INF/notice.txt",
                    "META-INF/ASL2.0",
                    "META-INF/INDEX.LIST"
                )
            )
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
        named("debug") {
            storeFile = file("key/${ProjectConfig.name}_debug.keystore")
            storePassword = "debug_key"
            keyAlias = ProjectConfig.name
            keyPassword = "debug_key"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["ApplicationId"] = ProjectConfig.applicationId + applicationIdSuffix
        }
        register("beta") {
            initWith(getByName("release"))
            matchingFallbacks += "release"

            buildConfigField("boolean", "IS_BETA", "true")

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            packaging {
                resources.excludes += "DebugProbesKt.bin"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    lint {
        checkReleaseBuilds = false
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

extensions.getByType<AppExtension>().apply {
    applicationVariants.all {
        addJavaSourceFoldersToModel(file("$buildDir/generated/ksp/$name/kotlin"))
    }
}

dependencies {
    implementation(project(path = ":Annotation"))
    ksp(project(path = ":AnnotationKSP"))

    // ATools
    val atoolsVersion = "0.0.25"
    implementation("io.github.xfy9326.atools:atools-io:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-ui:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-crash:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-coroutines:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-livedata:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-datastore-preference:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-datastore-preference-adapter:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-io-serialization-json:$atoolsVersion")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // AndroidX
    implementation("androidx.core:core-ktx:1.10.1")

    implementation("androidx.core:core-splashscreen:1.0.1")

    val appCompatVersion = "1.6.1"
    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("androidx.appcompat:appcompat-resources:$appCompatVersion")

    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // LifeCycle, ViewModel, LiveData
    val lifeCycleVersion = "2.6.1"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifeCycleVersion")

    // Room
    val roomVersion = "2.5.2"
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")

    // Material Design
    implementation("com.google.android.material:material:1.9.0")

    // Coil
    implementation("io.coil-kt:coil:2.4.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Ktor
    val ktorVersion = "2.3.3"
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Jsoup
    implementation("org.jsoup:jsoup:1.16.1")

    // ColorPicker
    implementation("com.jaredrummler:colorpicker:1.1.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    // androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // Debug
    debugImplementation("io.ktor:ktor-client-logging:$ktorVersion")

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.11")
}