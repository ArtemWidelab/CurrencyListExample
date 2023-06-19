plugins {
    id("android-hilt-library-convention")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("retrofit").get())
    implementation(libs.findLibrary("retrofitKotlinxSerialization").get())
    implementation(libs.findLibrary("kotlinxSerialization").get())
}