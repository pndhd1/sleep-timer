plugins {
    id(libs.plugins.android.application)
    id(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.metro)
}

android {
    namespace = BuildConfig.ApplicationId
    compileSdk {
        version = release(BuildConfig.CompileSdk)
    }

    defaultConfig {
        applicationId = BuildConfig.ApplicationId
        minSdk = BuildConfig.MinSdk
        targetSdk = BuildConfig.TargetSdk
        versionCode = BuildConfig.VersionCode
        versionName = BuildConfig.VersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        androidResources.localeFilters += listOf("en", "ru")
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
        sourceCompatibility = BuildConfig.Java
        targetCompatibility = BuildConfig.Java
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(BuildConfig.Jvm)
            freeCompilerArgs.add("-XXLanguage:+ContextParameters")
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.serivce)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.decompose)
    implementation(libs.decompose.extensions.compose)
    implementation(libs.essenty.lifecycle.coroutines)

    implementation(libs.kotlin.serialization.core)
    implementation(libs.kotlin.serialization.json)

}
