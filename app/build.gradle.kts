plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mobifog_fcm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mobifog_fcm"
        minSdk = 29
        //noinspection OldTargetApi
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    /*Aggiungo la dipendenza dalle librearie WorkManager, OkHttp e GSON (parsing json) per
    implementare l'invio e il parsing di richieste HTTP e FCM*/
    //noinspection GradleDependency
    implementation("androidx.work:work-runtime:2.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    //noinspection GradleDependency
    implementation("com.google.code.gson:gson:2.8.9")
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    //noinspection GradleDependency
    implementation("com.google.firebase:firebase-messaging:23.0.5")
}