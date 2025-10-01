
import com.android.build.gradle.internal.api.ApkVariantOutputImpl

plugins {
    alias(libs.plugins.self.application)
    alias(libs.plugins.self.compose)
    alias(libs.plugins.self.hilt)
    alias(libs.plugins.self.room)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.kotlin.serialization)
}

val baseAppName = "MMRL"
val mmrlBaseApplicationId = "com.dergoogler.mmrl"

val appVersion = commitCount + 31320

android {
    compileSdk = COMPILE_SDK
    namespace = mmrlBaseApplicationId

    defaultConfig {
        applicationId = namespace
        versionName = "v$appVersion"
        versionCode = appVersion

        androidResources.localeFilters += arrayOf(
            "en",
            "ar",
            "de",
            "es",
            "fr",
            "hi",
            "in",
            "it",
            "ja",
            "ta",
            "pl",
            "pt",
            "ro",
            "ru",
            "tr",
            "vi",
            "zh-rCN",
            "zh-rTW"
        )
    }

    val releaseSigning = if (project.hasReleaseKeyStore) {
        signingConfigs.create("release") {
            storeFile = project.releaseKeyStore
            storePassword = project.releaseKeyStorePassword
            keyAlias = project.releaseKeyAlias
            keyPassword = project.releaseKeyPassword
            enableV2Signing = true
            enableV3Signing = false
        }
    } else {
        signingConfigs.getByName("debug")
    }

    flavorDimensions += "distribution"

    productFlavors {
        create("official") {
            dimension = "distribution"
            applicationId = mmrlBaseApplicationId
            resValue("string", "app_name", baseAppName)
            buildConfigField("Boolean", "IS_SPOOFED_BUILD", "false")
        }

        create("spoofed") {
            dimension = "distribution"
            applicationId = generateRandomPackageName()
            resValue("string", "app_name", generateRandomName())
            buildConfigField("Boolean", "IS_SPOOFED_BUILD", "true")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_name", baseAppName)
            buildConfigField("Boolean", "IS_DEV_VERSION", "false")
            buildConfigField("Boolean", "IS_GOOGLE_PLAY_BUILD", "false")
            isDebuggable = false
            isJniDebuggable = false
            versionNameSuffix = "-release"
            renderscriptOptimLevel = 3
            multiDexEnabled = true

            manifestPlaceholders["webuiPermissionId"] = mmrlBaseApplicationId
        }

        create("playstore") {
            initWith(buildTypes.getByName("release"))
            matchingFallbacks += listOf("debug", "release")
            buildConfigField("Boolean", "IS_GOOGLE_PLAY_BUILD", "true")
            versionNameSuffix = "-playstore"
        }

        debug {
            resValue("string", "app_name", "$baseAppName Debug")
            buildConfigField("Boolean", "IS_DEV_VERSION", "true")
            buildConfigField("Boolean", "IS_GOOGLE_PLAY_BUILD", "false")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isJniDebuggable = true
            isDebuggable = true
            renderscriptOptimLevel = 0
            isMinifyEnabled = false
            multiDexEnabled = true

            manifestPlaceholders["webuiPermissionId"] = "$mmrlBaseApplicationId.debug"
        }

        all {
            signingConfig = releaseSigning

            buildConfigField("String", "COMPILE_SDK", "\"$COMPILE_SDK\"")
            buildConfigField("String", "BUILD_TOOLS_VERSION", "\"${BUILD_TOOLS_VERSION}\"")
            buildConfigField("String", "MIN_SDK", "\"$MIN_SDK\"")
            buildConfigField("String", "LATEST_COMMIT_ID", "\"${commitId}\"")

            manifestPlaceholders["__packageName__"] = mmrlBaseApplicationId}
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        jniLibs {
            pickFirsts += listOf("lib/arm64-v8a/libmmrl-file-manager.so")
        }

        resources {
            pickFirsts += setOf(
                "META-INF/gradle/incremental.annotation.processors"
            )

            excludes += setOf(
                "okhttp3/**",
                // "kotlin/**",
                "org/**",
                "**.properties",
                "**.bin",
                "**/*.proto"
            )
        }
    }

    dependenciesInfo.includeInApk = false

    applicationVariants.configureEach {
        outputs.configureEach {
            (this as? ApkVariantOutputImpl)?.outputFileName =
                "MMRL-$versionName-$flavorName.apk"
        }
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) {
        it.packaging.resources.excludes.add("META-INF/**")
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.swiperefreshlayout)
    compileOnly(projects.hiddenApi)
    implementation(projects.platform)
    implementation(projects.ui)
    implementation(projects.ext)
    implementation(projects.compat)
    implementation(projects.datastore)

    implementation(libs.webuix.portable)
    implementation(libs.webuix.helper)

    implementation(libs.kotlin.stdlib)

    implementation(libs.hiddenApiBypass)
    // implementation(libs.timber)
    implementation(libs.arbor.jvm)
    implementation(libs.arbor.android)

    implementation(libs.semver)
    implementation(libs.coil.compose)

    implementation(libs.libsu.core)
    implementation(libs.libsu.service)
    implementation(libs.libsu.io)

    implementation(libs.rikka.refine.runtime)
    implementation(libs.rikka.shizuku.api)
    implementation(libs.rikka.shizuku.provider)

    implementation(libs.apache.commons.compress)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.viewModel.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlin.reflect)
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.multiplatform.markdown.renderer.m3)
    implementation(libs.multiplatform.markdown.renderer.android)
    implementation(libs.multiplatform.markdown.renderer.coil3)
    implementation(libs.androidx.multidex)
    implementation(libs.dev.rikka.rikkax.parcelablelist)
    implementation(libs.lib.zoomable)
    implementation(libs.process.phoenix)
    // implementation(libs.androidx.adaptive)
    // implementation(libs.androidx.adaptive.android)
    // implementation(libs.androidx.adaptive.layout)
    // implementation(libs.androidx.adaptive.navigation)
    implementation(libs.kotlinx.html.jvm)

    implementation(libs.square.retrofit)
    implementation(libs.square.retrofit.moshi)
    implementation(libs.square.retrofit.kotlinxSerialization)
    implementation(libs.square.okhttp)
    implementation(libs.square.okhttp.dnsoverhttps)
    implementation(libs.square.logging.interceptor)
    implementation(libs.square.moshi)
    ksp(libs.square.moshi.kotlin)

    implementation("dev.chrisbanes.haze:haze:1.6.10")
    implementation("dev.chrisbanes.haze:haze-materials:1.6.10")

    implementation(libs.composedestinations.core)
    ksp(libs.composedestinations.ksp)
}

tasks.register("version") {
    println(appVersion)
}
