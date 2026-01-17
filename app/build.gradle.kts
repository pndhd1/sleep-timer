import java.util.Properties

plugins {
    id(libs.plugins.android.application)
    id(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.metro)
}

val keystoreProperties = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

fun getKeystoreProperty(envKey: String, propertyKey: String): String? {
    return System.getenv(envKey) ?: keystoreProperties.getProperty(propertyKey)
}

android {
    namespace = BuildConfig.ApplicationId
    compileSdk {
        version = release(BuildConfig.CompileSdk)
    }

    signingConfigs {
        create("release") {
            storeFile = file("${rootProject.projectDir}/keystore/release.jks")
            storePassword =
                getKeystoreProperty("SLEEPTIMER_RELEASE_STORE_PASSWORD", "storePassword")
            keyAlias = getKeystoreProperty("SLEEPTIMER_RELEASE_KEY_ALIAS", "keyAlias")
            keyPassword = getKeystoreProperty("SLEEPTIMER_RELEASE_KEY_PASSWORD", "keyPassword")
        }
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
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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

    packaging {
        resources.excludes += listOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "DebugProbesKt.bin",
            "META-INF/versions/9/previous-compilation-data.bin",
            "kotlin-tooling-metadata.json"
        )
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

    implementation(libs.flowext)
}
