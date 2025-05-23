import java.util.UUID

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.ali.englishlearning"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ali.englishlearning"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Enable multi-dex if needed
        multiDexEnabled = true

        // Add ndk filters for TensorFlow Lite
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
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

    // Enable view binding
    buildFeatures {
        viewBinding = true
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets")
        }
    }

    tasks.register("genUUID") {
        val outputDir = file("src/main/assets/model-en")
        outputs.dir(outputDir)

        doLast {
            outputDir.mkdirs()
            file("$outputDir/uuid").writeText(UUID.randomUUID().toString())
        }
    }

    tasks.named("preBuild") {
        dependsOn("genUUID")
    }

    dependencies {
        // Core Android libraries
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)

        // Firebase
        implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
        implementation("com.google.firebase:firebase-analytics-ktx")
        implementation("com.google.firebase:firebase-database:21.0.0")
        implementation("com.google.firebase:firebase-storage:21.0.1")
        implementation("com.google.firebase:firebase-auth:23.2.0")
        implementation("com.google.firebase:firebase-firestore:25.1.3")

        // Google services
        implementation("com.google.android.gms:play-services-ads-lite:24.0.0")
        implementation(libs.credentials)
        implementation(libs.credentials.play.services.auth)
        implementation(libs.googleid)

        // Image loading
        implementation("com.squareup.picasso:picasso:2.8")
        implementation("androidx.recyclerview:recyclerview:1.4.0")

        // JSON processing
        implementation("com.google.code.gson:gson:2.10.1")

        // TensorFlow Lite
        implementation("org.tensorflow:tensorflow-lite:2.14.0")
        implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
        implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
        implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")

        // Vosk (if needed)
        implementation("net.java.dev.jna:jna:5.13.0@aar")
        implementation("com.alphacephei:vosk-android:0.3.47@aar")

        // Multi-dex support
        implementation("androidx.multidex:multidex:2.0.1")

//        mlkit
        implementation ("com.google.mlkit:translate:17.0.1")

    }

// Fix for duplicate classes error
    configurations.all {
        resolutionStrategy {
            force("org.tensorflow:tensorflow-lite:2.14.0")
            force("org.tensorflow:tensorflow-lite-support:0.4.4")
            force("org.tensorflow:tensorflow-lite-metadata:0.4.4")
        }
    }
}

