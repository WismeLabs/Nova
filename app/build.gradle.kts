plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.wisme.nova"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wisme.nova"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // BuildConfig fields
        buildConfigField("String", "API_BASE_URL", "\"https://api.wisme.com/v1/\"")
        buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
        buildConfigField("int", "VERSION_CODE", "${defaultConfig.versionCode}")
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "BUILD_TYPE", "\"debug\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "BUILD_TYPE", "\"release\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Code quality configurations
    lint {
        warningsAsErrors = false
        abortOnError = false
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${rootProject.extra["lifecycle_version"]}")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime-livedata")

    // Navigation
    implementation("androidx.navigation:navigation-compose:${rootProject.extra["navigation_version"]}")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${rootProject.extra["lifecycle_version"]}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${rootProject.extra["lifecycle_version"]}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${rootProject.extra["lifecycle_version"]}")

    // Dependency Injection - Hilt
    implementation("com.google.dagger:hilt-android:${rootProject.extra["hilt_version"]}")
    kapt("com.google.dagger:hilt-compiler:${rootProject.extra["hilt_version"]}")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:${rootProject.extra["retrofit_version"]}")
    implementation("com.squareup.retrofit2:converter-gson:${rootProject.extra["retrofit_version"]}")
    implementation("com.squareup.okhttp3:okhttp:${rootProject.extra["okhttp_version"]}")
    implementation("com.squareup.okhttp3:logging-interceptor:${rootProject.extra["okhttp_version"]}")

    // Database - Room
    implementation("androidx.room:room-runtime:${rootProject.extra["room_version"]}")
    implementation("androidx.room:room-ktx:${rootProject.extra["room_version"]}")
    kapt("androidx.room:room-compiler:${rootProject.extra["room_version"]}")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:${rootProject.extra["firebase_bom_version"]}"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Media - ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer-core:${rootProject.extra["exoplayer_version"]}")
    implementation("com.google.android.exoplayer:exoplayer-ui:${rootProject.extra["exoplayer_version"]}")
    implementation("com.google.android.exoplayer:extension-mediasession:${rootProject.extra["exoplayer_version"]}")

    // Image Loading - Coil
    implementation("io.coil-kt:coil-compose:${rootProject.extra["coil_version"]}")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutines_version"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${rootProject.extra["coroutines_version"]}")

    // DataStore (for preferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Permission handling
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // System UI Controller
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${rootProject.extra["coroutines_version"]}")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:${rootProject.extra["hilt_version"]}")
    kaptAndroidTest("com.google.dagger:hilt-compiler:${rootProject.extra["hilt_version"]}")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Ktlint configuration
ktlint {
    version.set("0.50.0")
    debug.set(true)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

// Detekt configuration
detekt {
    config.setFrom("$projectDir/detekt.yml")
    buildUponDefaultConfig = true
}