plugins {
    id("android-hilt-library-convention")
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("room").get())
    implementation(libs.findLibrary("roomKtx").get())
    implementation(libs.findLibrary("result").get())
    kapt(libs.findLibrary("roomCompilerKapt").get())
    testImplementation(libs.findLibrary("roomTest").get())
}