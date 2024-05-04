plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.gallery"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.gallery"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        renderscriptSupportModeEnabled = true

        renderscriptTargetApi = 21
    }
    buildFeatures {
        viewBinding = true
        compose = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("jp.co.cyberagent.android.gpuimage:gpuimage-library:1.4.1")
    implementation("com.squareup.picasso:picasso:2.8")


    implementation("com.github.yalantis:ucrop:2.2.8-native")
    // For loading images
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.MikeOrtiz:TouchImageView:3.6")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

//    implementation("io.github.panpf.zoomimage:zoomimage-view-glide:1.0.2")
//    implementation("io.github.panpf.zoomimage:zoomimage-compose-glide:1.0.2")

    implementation("androidx.media3:media3-exoplayer:1.3.0")
    implementation("androidx.media3:media3-ui:1.3.0")
    implementation("androidx.media3:media3-common:1.3.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity:1.8.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")


    // Skip this if you don't want to use integration libraries or configure Glide.
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")
    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // ds-photo-editor-sdk (assuming it's an AAR library)
    implementation(files("libs/ds-photo-editor-sdk-v10.aar"))

    // SDK related dependencies
    implementation("io.reactivex.rxjava2:rxjava:2.1.0")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")




}