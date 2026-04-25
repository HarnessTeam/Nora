plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

android {
    namespace = "ai.nora"
    compileSdk = 36
    defaultConfig {
        applicationId = "ai.nora"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // Model packaging flavor dimension
    // - bundled: 模型文件 zip 压缩打包到 assets/，启动时提取到 filesDir/models/
    // - slim: 无内置模型，默认扫描 /data/local/tmp/llama/ 或用户 ADB 推送
    flavorDimensions += "model"
    productFlavors {
        create("bundled") {
            dimension = "model"
            buildConfigField("boolean", "MODEL_BUNDLED", "true")
            // assets/models/ 目录仅 bundled 变体使用
            sourceSets.getByName("main") {
                assets.srcDirs("src/main/assets")
            }
        }
        create("slim") {
            dimension = "model"
            buildConfigField("boolean", "MODEL_BUNDLED", "false")
            // slim 变体排除 assets/models/ 中的模型文件
            sourceSets {
                getByName("main") {
                    // 保留 assets 但不包含 models/ 子目录
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }

    lint {
      disable += "Instantiatable"
    }

    signingConfigs {
      create("release") {
        storeFile = file("${project.rootDir}/nora-release.jks")
        storePassword = "Nora2026!Release"
        keyAlias = "nora"
        keyPassword = "Nora2026!Release"
      }
    }

    buildTypes {
      release {
        isMinifyEnabled = false
        signingConfig = signingConfigs.getByName("release")
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      }
    }
  }

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Lifecycle
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  debugImplementation(libs.androidx.compose.ui.tooling)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Room
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)

  // SoLoader + fbjni (ExecuTorch dependencies)
  implementation(libs.soloader)
  implementation(libs.fbjni)

  // DataStore (persist modelLoaded state for startup navigation)
  implementation(libs.datastore.preferences)

  // ExecuTorch AAR (real LLM inference)
  implementation(libs.executorch)

  // Coroutines
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

  // Local tests
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)
}
