plugins {
    id("android-hilt-library-convention")
}

android {
    namespace = "ua.widelab.logger"
}

dependencies {
    implementation("io.github.aakira:napier:2.6.1")
    implementation(project(":android-initializable"))
}