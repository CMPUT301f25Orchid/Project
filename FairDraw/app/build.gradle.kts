plugins {
    alias(libs.plugins.android.application)
//    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fairdraw"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fairdraw"
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
}

dependencies {

    // existing dependencies...
    androidTestImplementation ("androidx.test:core:1.5.0")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test:runner:1.5.2")

    // Add Mockito for instrumentation tests
    androidTestImplementation ("org.mockito:mockito-android:5.5.0")


    testImplementation("org.mockito:mockito-inline:4.5.1")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.installations)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation(libs.material.v1120)
    androidTestImplementation("com.google.truth:truth:1.4.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
}