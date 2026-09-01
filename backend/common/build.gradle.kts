plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)

    alias(libs.plugins.spring.dependency.management)

    jacoco
}

dependencies {
    implementation(project(":data"))

    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)

    implementation(libs.jakarta.validation.api)
    implementation(libs.springdoc.openapi.starter.common)
    implementation(libs.hibernate.core)
}

tasks.test {
    useJUnitPlatform()
}
