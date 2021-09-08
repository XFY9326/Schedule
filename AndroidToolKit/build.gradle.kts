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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    api(project(path = ":ToolKit"))

    api(group = "androidx.annotation", name = "annotation", version = Dependencies.androidx_annotation)

    api(group = "androidx.core", name = "core-ktx", version = Dependencies.androidx_core)
    api(group = "androidx.appcompat", name = "appcompat", version = Dependencies.androidx_appcompat)

    api(group = "androidx.activity", name = "activity-ktx", version = Dependencies.androidx_activity)
    api(group = "androidx.fragment", name = "fragment-ktx", version = Dependencies.androidx_fragment)

    api(group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version = Dependencies.androidx_lifecycle)
    api(group = "androidx.lifecycle", name = "lifecycle-common-java8", version = Dependencies.androidx_lifecycle)

    testImplementation(group = "junit", name = "junit", version = Dependencies.junit)
    androidTestImplementation(group = "androidx.test.ext", name = "junit", version = Dependencies.androidx_junit)
}