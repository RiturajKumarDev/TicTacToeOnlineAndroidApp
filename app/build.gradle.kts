plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.rituraj.tictactoeonlineapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rituraj.tictactoeonlineapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 17
        versionName = "2.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.firebase.ai)
    implementation(libs.legacy.support.v4)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    implementation(libs.fragment)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.navigation.fragment)
    implementation(libs.androidx.navigation.navigation.ui)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.navigation.navigation.fragment2)
    implementation(libs.androidx.navigation.navigation.ui2)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase BOM (versions automatic handle honge)
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")

    // Image handling
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation ("com.google.android.gms:play-services-auth:21.2.0")


    implementation("com.google.errorprone:error_prone_annotations:2.16")
    
    // Google Play Service
    implementation("com.google.android.gms:play-services-games-v2:+")
}