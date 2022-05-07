import com.android.build.api.variant.impl.VariantImpl
import com.android.build.api.variant.impl.VariantOutputImpl

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
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

        androidComponents {
            onVariants { variants ->
                if (variants is VariantImpl) {
                    variants.variantData.addJavaSourceFoldersToModel(file("$buildDir/generated/ksp/${variants.buildType}/kotlin"))
                }
                variants.outputs.forEach {
                    if (it is VariantOutputImpl && variants.buildType != "debug") {
                        it.outputFileName.set("${ProjectConfig.name}_v${versionName}_${versionCode}_${variants.buildType}.apk")
                    }
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
    }

    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
        getByName("debug") {
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

            packagingOptions {
                resources.excludes += "DebugProbesKt.bin"
            }
        }
    }

    compileOptions {
        sourceCompatibility = ProjectConfig.javaVersion
        targetCompatibility = ProjectConfig.javaVersion
    }

    kotlinOptions {
        jvmTarget = ProjectConfig.javaVersion.toString()
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
    }

    lint {
        checkReleaseBuilds = false
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(project(path = ":Annotation"))
    ksp(project(path = ":AnnotationKSP"))

    // ATools
    val atoolsVersion = "0.0.7"
    implementation("io.github.xfy9326.atools:atools-io:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-ui:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-crash:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-coroutines:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-livedata:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-datastore-preference:$atoolsVersion")
    implementation("io.github.xfy9326.atools:atools-io-serialization-json:$atoolsVersion")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    // AndroidX
    implementation("androidx.core:core-ktx:1.7.0")

    implementation("androidx.core:core-splashscreen:1.0.0-beta02")

    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.appcompat:appcompat-resources:1.4.1")

    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")

    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // LifeCycle, ViewModel, LiveData
    val lifeCycleVersion = "2.4.1"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifeCycleVersion")

    // Room
    val roomVersion = "2.4.2"
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")

    // Material Design
    implementation("com.google.android.material:material:1.6.0")

    // Coil
    implementation("io.coil-kt:coil:1.4.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // Ktor
    val ktorVersion = "2.0.1"
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Jsoup
    implementation("org.jsoup:jsoup:1.14.3")

    // ColorPicker
    implementation("com.jaredrummler:colorpicker:1.1.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    // androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // Debug
    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.6")
}