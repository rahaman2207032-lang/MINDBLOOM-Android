plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mindbloomandroid"

    // ✅ FIXED:  Corrected compileSdk syntax
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mindbloomandroid"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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

    // ✅ RECOMMENDED: Enable view binding for easier UI code
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // ====== ANDROID UI DEPENDENCIES (from version catalog) ======
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")


    implementation("com.google.firebase:firebase-database")


    implementation("com.google.firebase:firebase-storage")


    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)


    implementation("androidx.recyclerview:recyclerview:1.3.2")


    implementation("androidx.cardview:cardview:1.0.0")


    implementation("androidx.viewpager2:viewpager2:1.0.0")


    implementation("androidx.fragment:fragment-ktx:1.6.2")


    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.firebase.database)


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}