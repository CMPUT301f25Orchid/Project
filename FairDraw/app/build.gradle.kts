import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.external.javadoc.StandardJavadocDocletOptions

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

tasks.register<Javadoc>("androidJavadoc") {
    // Use the same sources & classpath as the debug Java compile
    val javaCompile = tasks.named("compileDebugJavaWithJavac", JavaCompile::class).get()

    dependsOn(javaCompile)

    // Sources (includes anything the debug variant compiles)
    source = javaCompile.source

    // Classpath: whatever debug compile uses + Android boot classpath
    classpath = javaCompile.classpath + files(android.bootClasspath)

    // Where to put the generated docs
    setDestinationDir(file("$rootDir/docs/javadoc"))

    // Make Javadoc less strict / nicer
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        charSet = "UTF-8"
        addStringOption("Xdoclint:none", "-quiet")
        links("https://developer.android.com/reference/")
    }
}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.installations)
    implementation(libs.firebase.storage)
    implementation(libs.espresso.intents)
    implementation(libs.cardview)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.7.0") {
        exclude("com.google.protobuf", "protobuf-lite")
    }
    testImplementation(libs.junit)
    // Robolectric for JVM unit tests
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("androidx.test:core:1.5.0")
    // Mockito for JVM unit tests
    testImplementation("org.mockito:mockito-core:4.11.0")
    // Mockito-inline helps mock final classes if needed
    testImplementation("org.mockito:mockito-inline:4.11.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("com.google.truth:truth:1.4.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Add this line to resolve the intent errors
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}