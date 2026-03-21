import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

val projectName = "PureSchedule"

android {
    namespace = "tool.xfy9326.schedule"
    compileSdk = 36

    defaultConfig {
        applicationId = "tool.xfy9326.schedule"
        minSdk = 23
        targetSdk = 36
        versionCode = 42
        versionName = "1.5.2"

        buildConfigField("String", "BASE_APPLICATION_ID", "\"${applicationId.orEmpty()}\"")
        buildConfigField("String", "PROJECT_NAME", "\"${projectName}\"")
        buildConfigField("boolean", "ENABLE_UPDATE_CHECK", "false")
        manifestPlaceholders["ApplicationId"] = applicationId.orEmpty()
        manifestPlaceholders["BaseApplicationId"] = applicationId.orEmpty()

        androidResources {
            @Suppress("UnstableApiUsage")
            localeFilters += "zh"
        }

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
        buildConfig = true
    }

    signingConfigs {
        named("debug") {
            storeFile = file("key/${projectName}_debug.keystore")
            storePassword = "debug_key"
            keyAlias = projectName
            keyPassword = "debug_key"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["ApplicationId"] = defaultConfig.applicationId + applicationIdSuffix
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

    lint {
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(project(path = ":Annotation"))
    ksp(project(path = ":AnnotationKSP"))

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    // AndroidX
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.startup:startup-runtime:1.2.0")
    implementation("androidx.core:core-splashscreen:1.2.0")

    val appCompatVersion = "1.7.1"
    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("androidx.appcompat:appcompat-resources:$appCompatVersion")

    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // LifeCycle, ViewModel, LiveData
    val lifeCycleVersion = "2.10.0"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifeCycleVersion")

    // Room
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")

    // Material Design
    implementation("com.google.android.material:material:1.13.0")

    // Coil
    implementation("io.coil-kt.coil3:coil:3.4.0")

    // OkHttp
    implementation("com.squareup.okio:okio:3.17.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    // Ktor
    val ktorVersion = "3.4.1"
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Jsoup
    implementation("org.jsoup:jsoup:1.22.1")

    // ColorPicker
    implementation("com.jaredrummler:colorpicker:1.1.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    // androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Debug
    debugImplementation("io.ktor:ktor-client-logging:$ktorVersion")

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}