plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.lemonview.ai"
    compileSdk = 34

    android {
        defaultConfig {
            applicationId = "com.lemonview.ai"
            minSdk = 23
            targetSdk = 34
            versionCode = 2
            versionName = "1.1"
        }

    }



    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    
    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Generative AI - Gemini API
    implementation("com.google.ai.client.generativeai:generativeai:0.5.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // CameraX
    // CameraX (REQUIRED)
    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")
    implementation("com.google.guava:guava:33.2.0-android")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // TensorFlow Lite for on-device ML
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    
    // ONNX Runtime for acne detection model
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.16.0")
    
    // ML Kit Face Detection for facial landmarks
    implementation("com.google.mlkit:face-detection:16.1.5")

}