plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    id("io.freefair.lombok") version "8.6"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

val lombokVersion = "1.18.+"
val mockitoVersion =  "5.11.+"

dependencies {
    // Existing dependencies remain unchanged
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    api(libs.commons.math3)
    implementation(libs.guava)

    // Lombok dependencies using the single version variable
    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    // Mockito dependencies
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.mockito:mockito-junit-jupiter:${mockitoVersion}")

}

dependencyLocking {
    lockAllConfigurations()
}