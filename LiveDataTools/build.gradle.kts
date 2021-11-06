plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = PROJECT_JAVA_VERSION
        targetCompatibility = PROJECT_JAVA_VERSION
    }

    kotlinOptions {
        jvmTarget = PROJECT_JAVA_VERSION.toString()
    }
}

dependencies {
    api(group = "androidx.lifecycle", name = "lifecycle-livedata-ktx", version = Dependencies.androidx_lifecycle)

    testImplementation(group = "junit", name = "junit", version = Dependencies.junit)
    androidTestImplementation(group = "androidx.test.ext", name = "junit", version = Dependencies.androidx_junit)
}