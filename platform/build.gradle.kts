import java.security.SecureRandom

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dergoogler.mmrl.platform"
    compileSdk = 36

    publishing {
        singleVariant("release")
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            abiFilters += arrayOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DANDROID_STL=c++_static",
                    "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON",
                )
            }
        }
        val aidlDir = file("src/main/aidl")
        aidlPackagedList += aidlDir.walkTopDown()
            .filter { it.isFile && it.extension == "aidl" }
            .map { it.relativeTo(aidlDir).path }
            .toList()
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    splits {
        abi {
            isEnable = false
            isUniversalApk = false
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

    buildFeatures {
        buildConfig = true
        compose = true
        aidl = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    compileOnly(projects.hiddenApi)
    implementation(projects.ext)
    implementation(projects.compat)
    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.hiddenApiBypass)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.apache.commons.compress)
    implementation(libs.square.retrofit.moshi)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.square.moshi)
    ksp(libs.square.moshi.kotlin)
}

// AGP 9.0 applies KGP internally without going through Gradle's plugin manager, which prevents
// KotlinCompilerPluginSupportPlugin.applyToCompilation() from being called for kotlin.parcelize.
// Use configurations.all (not afterEvaluate + configurations.names) so all variant configs are covered.
val parcelizeVersion = libs.versions.kotlin.get()
configurations.all {
    if (name.startsWith("kotlinCompilerPluginClasspath")) {
        project.dependencies.add(name, "org.jetbrains.kotlin:kotlin-parcelize-compiler:$parcelizeVersion")
    }
}

fun generateRandomName(
    minLength: Int = 5,
    maxLength: Int = 12,
): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val random = SecureRandom()
    val length = random.nextInt(maxLength - minLength + 1) + minLength
    return (1..length)
        .map { chars[random.nextInt(chars.length)] }
        .joinToString("")
}