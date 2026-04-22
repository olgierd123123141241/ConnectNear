plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

apply(from = "local.properties.gradle.kts")
val localProperties: java.util.Properties by project.extra

android {
    namespace = "pl.example.connectnear"
    compileSdk = 35 

    defaultConfig {
        applicationId = "pl.example.connectnear"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "2.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY") ?: ""
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    // --- Standardowe biblioteki Androida ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Używamy stabilnego BOM dla Compose
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    
    implementation(libs.youtube.player)
    implementation(libs.coil.compose)
    
    // --- FIREBASE (z użyciem BOM dla spójności wersji) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.play.services.auth)
    implementation(libs.facebook.login)

    // --- MAPY I LOKALIZACJA ---
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    //implementation(libs.firebase.firestore.ktx) // Ta linia może być zbędna w nowych wersjach
}
