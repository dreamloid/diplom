@file:Suppress("UNUSED_EXPRESSION", "DEPRECATION")

import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("kotlin-kapt")
}

android {
    namespace = "com.example.diplom"
    compileSdk = 34
    buildFeatures{
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.example.diplom"
        minSdk = 28
        targetSdk = 34
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

//    buildFeatures{
//        viewBinding = true
//    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packagingOptions {
        // Добавьте следующую строку для вызова функции resources
        resourcesBlock()
    }
}
fun Packaging.resourcesBlock() {
    excludes.add("META-INF/NOTICE.md")
    excludes.add("META-INF/LICENSE.md") // Добавьте исключение для 'META-INF/LICENSE.md'
}
dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity:1.8.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.sun.mail:android-mail:1.6.7") {
        exclude(group = "javax.activation", module = "activation")
    }
    implementation("com.sun.mail:android-activation:1.6.7") {
        exclude(group = "javax.activation", module = "activation")
    }
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Добавление зависимостей для Glide
    //implementation("com.github.bumptech.glide:glide:4.12.0")
    //kapt("com.github.bumptech.glide:compiler:4.12.0")
}