plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kspPlugin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.ask.admin"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    buildFeatures {
        buildConfig = true
        compose = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.androidx.ui.navigation)
    implementation(libs.bundles.hilt.navigation.work)
    implementation(libs.kotlinx.serialization.json)
    ksp(libs.hilt.compiler)

    implementation(libs.generativeai)
    implementation(libs.jsoup)
    implementation(libs.coil.compose)

    implementation(project(":feature:common"))
    implementation(project(":domain:widget"))
    implementation(project(":data:widget"))
    implementation(project(":data:user"))
    implementation(project(":workmanager"))
    implementation(project(":data:category"))
    implementation(project(":domain:category"))
    implementation(project(":data:core"))
    implementation(project(":domain:common"))
    implementation(project(":domain:country"))
    implementation(project(":data:country"))
    implementation(project(":data:analytics"))


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}