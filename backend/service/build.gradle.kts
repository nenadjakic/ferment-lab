plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)

    alias(libs.plugins.spring.dependency.management)

    jacoco
}

dependencies {
    implementation(project(":data"))
    implementation(project(":common"))

    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)

    implementation(libs.jakarta.persistence.api)
    implementation(libs.jakarta.transaction.api)
    implementation(libs.jakarta.validation.api)
    implementation(libs.spring.data.jpa)
    implementation(libs.hibernate.core)
}

tasks.test {
    useJUnitPlatform()
}
