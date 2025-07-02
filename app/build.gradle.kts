import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

fun getLocalProperty(key: String, defaultValue: String = ""): String {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(FileInputStream(localPropertiesFile))
    }
    return properties.getProperty(key, defaultValue)
}

android {
    namespace = "com.netease.nim.samples"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.netease.nim.samples"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "APP_KEY", "\"${getLocalProperty("appKey")}\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.multidex)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    implementation(libs.android.pickerview)
    implementation(libs.core.ktx)
    implementation(libs.activity)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 基础库，必须依赖
    implementation(libs.nimlib.basesdk)
    //聊天室插件
    implementation(libs.nimlib.chatroom)
    // 超大群插件
    implementation(libs.nimlib.superteam)
    // 全文检索插件
    implementation(libs.nimlib.lucene)
    // 推送插件
    implementation(libs.nimlib.push)

    // ViewModel and LiveData
    implementation(libs.lifecycle.extensions)
    // Java8 support for Lifecycles
    implementation(libs.lifecycle.common.java8)

}