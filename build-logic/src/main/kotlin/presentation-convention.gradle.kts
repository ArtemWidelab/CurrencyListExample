plugins {
    id("android-hilt-library-convention")
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")



dependencies {
    implementation(libs.findLibrary("androidLifecycle").get())
    implementation(libs.findLibrary("androidViewModel").get())
    implementation(libs.findLibrary("androidComposeViewModel").get())
    api(libs.findLibrary("immutableCollections").get())
    kapt(libs.findLibrary("androidLifecycleCompilerKapt").get())
}