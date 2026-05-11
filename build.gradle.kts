plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.circadianx.sleepsense"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.circadianx.sleepsense"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Read MAPS_API_KEY from local.properties (or env). Falls back to empty so the build still succeeds.
        val mapsKey: String = providers.gradleProperty("MAPS_API_KEY").orNull
            ?: System.getenv("MAPS_API_KEY")
            ?: ""
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey

        // Backend base URL — override in local.properties: BACKEND_URL=http://192.168.x.x:8080
        val backendUrl: String = providers.gradleProperty("BACKEND_URL").orNull
            ?: System.getenv("BACKEND_URL")
            ?: "https://api.sleepsense.app"
        buildConfigField("String", "BACKEND_URL", "\"$backendUrl\"")

        val googleWebClientId: String = providers.gradleProperty("GOOGLE_WEB_CLIENT_ID").orNull
            ?: System.getenv("GOOGLE_WEB_CLIENT_ID")
            ?: ""
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")

        val spotifyClientId: String = providers.gradleProperty("SPOTIFY_CLIENT_ID").orNull
            ?: System.getenv("SPOTIFY_CLIENT_ID")
            ?: ""
        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"$spotifyClientId\"")

        val spotifyRedirectUri: String = providers.gradleProperty("SPOTIFY_REDIRECT_URI").orNull
            ?: System.getenv("SPOTIFY_REDIRECT_URI")
            ?: "sleepsense://spotify-auth"
        buildConfigField("String", "SPOTIFY_REDIRECT_URI", "\"$spotifyRedirectUri\"")
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["allowCleartext"] = "true"
            isMinifyEnabled = false
        }
        release {
            manifestPlaceholders["allowCleartext"] = "false"
            isMinifyEnabled = true
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.health.connect)

    implementation(libs.coil.compose)
    implementation(libs.vico.compose.m3)

    implementation(libs.play.services.location)
    implementation(libs.play.services.tasks)
    implementation(libs.maps.compose)
    implementation(libs.maps.utils)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    debugImplementation(libs.androidx.ui.tooling)
}
