plugins {
    id("persistance-convention")
}

android {
    namespace = "ua.widelab.currency.persistence"
}

dependencies {
    api(project(":currency-entities"))
}