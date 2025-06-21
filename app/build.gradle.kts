plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "kr.ac.uc.test_2025_05_19_k"
    compileSdk = 34

    defaultConfig {
        applicationId = "kr.ac.uc.test_2025_05_19_k"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties"
            )
        }
    }


}



dependencies {

        // Kotlin & Coroutines
        implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        implementation("androidx.compose.runtime:runtime-livedata:1.6.7")

        // Jetpack Compose (BOM 방식)
        implementation(platform("androidx.compose:compose-bom:2024.09.00"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.material3:material3:1.2.1")
        implementation("androidx.compose.material:material-icons-core:1.6.7")
        implementation("androidx.compose.material:material-icons-extended:1.6.7")
        implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.firebase.appdistribution.gradle)

    debugImplementation("androidx.compose.ui:ui-tooling")

        // AppCompat
        implementation("androidx.appcompat:appcompat:1.7.0")

        // Lifecycle
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

        // Activity Compose
        implementation("androidx.activity:activity:1.9.0")
        implementation("androidx.activity:activity-ktx:1.9.0")
        implementation("androidx.activity:activity-compose:1.9.0")

        // Navigation Compose
        implementation("androidx.navigation:navigation-compose:2.7.7")

        // Hilt
        implementation("com.google.dagger:hilt-android:2.47")
        kapt("com.google.dagger:hilt-android-compiler:2.47")
        implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
        kapt("androidx.hilt:hilt-compiler:1.0.0")

        // Javapoet (필요시)
        implementation("com.squareup:javapoet:1.13.0")

        // Retrofit
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

        // Coil (이미지 로딩)
        implementation("io.coil-kt:coil-compose:2.2.2")

        // 기타
        implementation("androidx.browser:browser:1.7.0")
        implementation("com.google.accompanist:accompanist-permissions:0.34.0")
        implementation("com.google.android.gms:play-services-location:21.2.0")

    implementation ("com.google.android.gms:play-services-location:21.0.1")


}






