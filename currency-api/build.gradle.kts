plugins {
    id("network-convention")
}

android {
    namespace = "ua.widelab.currency.api"
}

dependencies {
    api(project(":currency-entities"))
    api(project(":app-network"))
    api(libs.result)
}