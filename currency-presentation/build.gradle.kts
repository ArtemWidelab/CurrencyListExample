plugins {
    id("presentation-convention")
}

android {
    namespace = "ua.widelab.currency.presentation"
}

dependencies {
    implementation(libs.result)
    api(project(":currency-repo"))
}