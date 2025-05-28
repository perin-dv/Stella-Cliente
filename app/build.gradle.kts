plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.apkstelladitalia20"
    compileSdk = 36


    repositories {
        flatDir {
            dirs("libs")
        }
    }

    defaultConfig {
        applicationId = "com.example.apkstelladitalia20"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        viewBinding = true
    }
}

repositories {
    google()
    mavenCentral()

    // ✅ ESSENCIAL PARA LOCAL .aar
    flatDir {
        dirs("libs")
    }
}

dependencies {
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.analytics.ktx)
    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.google.firebase.storage.ktx)
    implementation(libs.google.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation("androidx.appcompat:appcompat:1.6.1")




    // AndroidX
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.support.annotations)
    implementation(libs.play.services.location)

    // Outras libs
    implementation(libs.glide)
    implementation(libs.shimmer)
    implementation(libs.picasso)
    implementation(libs.gson)






    // Room
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    kapt("androidx.room:room-compiler:2.7.0")
    implementation("com.google.firebase:firebase-functions-ktx:20.3.1")


    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    kapt(libs.compiler)
}
