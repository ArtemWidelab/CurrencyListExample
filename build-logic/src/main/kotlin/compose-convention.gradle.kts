plugins {
    id("android-hilt-library-convention")
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")

android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion =
            libs.findVersion("composeKotlinCompiler").get().requiredVersion
    }
}

dependencies {
    implementation(platform(libs.findLibrary("composeBom").get()))
    implementation(libs.findLibrary("composeActivity").get())
    implementation(libs.findLibrary("composeUi").get())
    implementation(libs.findLibrary("composeUiGraphics").get())
    implementation(libs.findLibrary("composeUiToolingPreview").get())
    implementation(libs.findLibrary("composeMaterial3").get())
    implementation(libs.findLibrary("androidComposeViewModel").get())
}