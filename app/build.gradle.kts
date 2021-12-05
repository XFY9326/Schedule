import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
}

android {
    compileSdk = Android.compileSdk

    defaultConfig {
        applicationId = Android.applicationId
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = Android.versionCode
        versionName = Android.versionName

        resourceConfigurations.clear()
        resourceConfigurations.add("zh")

        buildConfigField("String", "BASE_APPLICATION_ID", "\"${Android.applicationId}\"")
        buildConfigField("String", "PROJECT_NAME", "\"$ProjectName\"")
        buildConfigField("boolean", "IS_BETA", "false")
        manifestPlaceholders["ApplicationId"] = Android.applicationId
        manifestPlaceholders["BaseApplicationId"] = Android.applicationId

        applicationVariants.all {
            outputs.all {
                if (this is BaseVariantOutputImpl && buildType.name != "debug") {
                    outputFileName = "${ProjectName}_v${versionName}_${versionCode}_${GitCommitShortId}_${buildType.name}.apk"
                }
            }
        }

        packagingOptions {
            resources.excludes.apply {
                add("META-INF/*.version")
                add("META-INF/CHANGES")
                add("META-INF/README.md")
                add("okhttp3/internal/publicsuffix/NOTICE")
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
        withDebug {
            storeFile = file("key/${ProjectName}_debug.keystore")
            storePassword = "debug_key"
            keyAlias = ProjectName
            keyPassword = "debug_key"
        }
    }

    buildTypes {
        withDebug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["ApplicationId"] = Android.applicationId + applicationIdSuffix
        }
        register("beta") {
            initWith(getByName("release"))
            buildConfigField("boolean", "IS_BETA", "true")

            versionNameSuffix = "-$GitCommitShortId"

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            matchingFallbacks.add("release")
        }
        withRelease {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            packagingOptions {
                resources.excludes.add("DebugProbesKt.bin")
            }
        }
    }

    compileOptions {
        sourceCompatibility = PROJECT_JAVA_VERSION
        targetCompatibility = PROJECT_JAVA_VERSION
    }

    kotlinOptions {
        jvmTarget = PROJECT_JAVA_VERSION.toString()
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
    }

    lint {
        isCheckReleaseBuilds = false
    }
}

dependencies {
    implementation(project(path = ":Annotation"))
    kapt(project(path = ":AnnotationProcessor"))

    implementation(project(path = ":LiveDataTools"))
    implementation(project(path = ":AndroidToolKit"))
    implementation(project(path = ":AndroidToolKitIO"))

    // Kotlin
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version = Dependencies.kotlinx_coroutines)

    // Kotlin Serialization
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = Dependencies.kotlinx_serialization)

    // AndroidX
    implementation(group = "androidx.core", name = "core-ktx", version = Dependencies.androidx_core)

    implementation(group = "androidx.core", name = "core-splashscreen", version = "1.0.0-alpha02")

    implementation(group = "androidx.appcompat", name = "appcompat", version = Dependencies.androidx_appcompat)
    implementation(group = "androidx.appcompat", name = "appcompat-resources", version = Dependencies.androidx_appcompat)

    implementation(group = "androidx.activity", name = "activity-ktx", version = Dependencies.androidx_activity)
    implementation(group = "androidx.fragment", name = "fragment-ktx", version = Dependencies.androidx_fragment)

    implementation(group = "androidx.drawerlayout", name = "drawerlayout", version = "1.1.1")
    implementation(group = "androidx.constraintlayout", name = "constraintlayout", version = "2.1.0")

    implementation(group = "androidx.preference", name = "preference-ktx", version = "1.1.1")
    implementation(group = "androidx.datastore", name = "datastore-preferences", version = "1.0.0")

    // LifeCycle, ViewModel, LiveData
    implementation(group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version = Dependencies.androidx_lifecycle)
    implementation(group = "androidx.lifecycle", name = "lifecycle-common-java8", version = Dependencies.androidx_lifecycle)
    implementation(group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version = Dependencies.androidx_lifecycle)
    implementation(group = "androidx.lifecycle", name = "lifecycle-livedata-ktx", version = Dependencies.androidx_lifecycle)

    // Room
    implementation(group = "androidx.room", name = "room-runtime", version = Dependencies.androidx_room)
    implementation(group = "androidx.room", name = "room-ktx", version = Dependencies.androidx_room)
    kapt(group = "androidx.room", name = "room-compiler", version = Dependencies.androidx_room)

    // Material Design
    implementation(group = "com.google.android.material", name = "material", version = "1.4.0")

    // Coil
    implementation(group = "io.coil-kt", name = "coil", version = "1.4.0")

    // OkHttp
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.9.3")

    // Ktor
    implementation(group = "io.ktor", name = "ktor-client-okhttp", version = Dependencies.ktor)
    implementation(group = "io.ktor", name = "ktor-client-serialization", version = Dependencies.ktor)

    // Jsoup
    implementation(group = "org.jsoup", name = "jsoup", version = "1.14.3")

    // ColorPicker
    implementation(group = "com.jaredrummler", name = "colorpicker", version = "1.1.0")

    // Test
    testImplementation(group = "junit", name = "junit", version = Dependencies.junit)
    testImplementation(group = "androidx.room", name = "room-testing", version = Dependencies.androidx_room)
    androidTestImplementation(group = "androidx.test.ext", name = "junit", version = Dependencies.androidx_junit)
    androidTestImplementation(group = "androidx.test.espresso", name = "espresso-core", version = Dependencies.androidx_espresso)

    // Debug
    // debugImplementation(group = "com.squareup.leakcanary", name = "leakcanary-android", version = "2.6")
}