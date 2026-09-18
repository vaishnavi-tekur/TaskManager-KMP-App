import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

<<<<<<< HEAD
=======
// Read properties from .env file
>>>>>>> task/kmp-task-scheduling
val envFile = project.rootProject.file(".env")
val env = Properties()
if (envFile.exists()) {
    envFile.inputStream().use { env.load(it) }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

android {
    namespace = "com.example.taskmanagerkmpapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.taskmanagerkmpapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

<<<<<<< HEAD
=======
        // Inject variables into BuildConfig
>>>>>>> task/kmp-task-scheduling
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${env.getProperty("GOOGLE_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "BACKEND_URL", "\"${env.getProperty("BACKEND_URL") ?: ""}\"")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}
