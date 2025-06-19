plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.unigo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.unigo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        buildFeatures {
            viewBinding = true
            dataBinding = true
            buildConfig = true
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
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        //noinspection WrongGradleMethod
        kotlinOptions {
            jvmTarget = "17"
        }

        lint {
            abortOnError = false
            checkReleaseBuilds = false
            disable += setOf("ObsoleteLintCustomCheck")
        }
    }

    dependencies {

        // AndroidX Core
        implementation("androidx.core:core-ktx:1.10.1")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.9.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("androidx.legacy:legacy-support-v4:1.0.0")
        implementation("androidx.vectordrawable:vectordrawable:1.1.0")
        implementation("androidx.vectordrawable:vectordrawable-animated:1.1.0")

        // Navigation
        implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
        implementation("androidx.navigation:navigation-ui-ktx:2.5.3")

        // Firebase
        implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
        implementation("com.google.firebase:firebase-analytics-ktx")
        implementation("com.google.firebase:firebase-auth-ktx")
        implementation("com.google.firebase:firebase-firestore-ktx")
        implementation("com.google.firebase:firebase-storage-ktx")
        implementation("com.google.firebase:firebase-database-ktx")
        implementation("com.google.android.gms:play-services-auth:20.4.1")

        // Lifecycle
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")


        // Retrofit & OkHttp
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("com.squareup.okhttp3:okhttp:4.10.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
        implementation("com.google.code.gson:gson:2.9.0")

        // Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

        // Glide
        implementation("com.github.bumptech.glide:glide:4.14.2")

        // Test
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
        // Google AI
        implementation("com.google.ai.client.generativeai:generativeai:0.1.2")
        implementation("com.google.code.gson:gson:2.10.1")
    }
}
dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
}
