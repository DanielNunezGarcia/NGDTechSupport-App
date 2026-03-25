plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

fun loadLocalProperties(): Map<String, String> {
    val props = mutableMapOf<String, String>()
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.readLines()
            .filter { it.contains("=") && !it.trim().startsWith("#") }
            .forEach { line ->
                val parts = line.split("=", limit = 2)
                props[parts[0].trim()] = parts[1].trim()
            }
    }
    return props
}

val localProps = loadLocalProperties()

fun resolveProperty(name: String): String? =
    providers.gradleProperty(name).orNull
        ?: System.getenv(name)
        ?: localProps[name]?.takeIf { it.isNotBlank() }

val releaseSigningStoreFile = resolveProperty("SIGNING_STORE_FILE")?.takeIf { it.isNotBlank() }
val releaseSigningStorePassword = resolveProperty("SIGNING_STORE_PASSWORD")?.takeIf { it.isNotBlank() }
val releaseSigningKeyAlias = resolveProperty("SIGNING_KEY_ALIAS")?.takeIf { it.isNotBlank() }
val releaseSigningKeyPassword = resolveProperty("SIGNING_KEY_PASSWORD")?.takeIf { it.isNotBlank() }

val hasReleaseSigning = listOf(
    releaseSigningStoreFile,
    releaseSigningStorePassword,
    releaseSigningKeyAlias,
    releaseSigningKeyPassword
).all { it != null }

if (!hasReleaseSigning) {
    logger.warn(
        "Release signing credentials are incomplete. Falling back to debug signing for release builds. Configure CI signing vars for production artifacts."
    )
}

android {
    namespace = "com.example.ngdtechsupport"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ngdtechsupport"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = rootProject.file(releaseSigningStoreFile!!)
                storePassword = releaseSigningStorePassword
                keyAlias = releaseSigningKeyAlias
                keyPassword = releaseSigningKeyPassword
            } else {
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "ENABLE_FIREBASE_MONITORING", "false")
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("boolean", "ENABLE_FIREBASE_MONITORING", "true")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging:24.0.3")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-appcheck-debug")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
}
