plugins {
    id("compose-convention")
}

android {
    namespace = "ua.widelab.currency.compose"
}

dependencies {
    api(project(":currency-presentation"))
    implementation(project(":compose-components"))
}