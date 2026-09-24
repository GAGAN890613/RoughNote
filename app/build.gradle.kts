plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.gagan.roughnotes"
    compileSdk = 35
    defaultConfig { applicationId = "com.gagan.roughnotes"; minSdk = 26; targetSdk = 28; versionCode = 1; versionName = "1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
