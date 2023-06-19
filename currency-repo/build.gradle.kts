plugins {
    id("android-hilt-library-convention")
}

android {
    namespace = "ua.widelab.currency.repo"
}

dependencies {
    implementation(libs.dataStorePreferences)
    implementation(libs.result)
    implementation(project(":currency-api"))
    implementation(project(":currency-persistence"))
    api(project(":currency-entities"))
}