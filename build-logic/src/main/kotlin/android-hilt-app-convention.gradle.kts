import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("android-app-convention")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")


dependencies {
    implementation(libs.findLibrary("hiltAndroid").get())
    kapt(libs.findLibrary("hiltKapt").get())
}

kapt {
    correctErrorTypes = true
}

hilt {
    enableAggregatingTask = true
}