plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    id("io.freefair.lombok") version "8.6"
    id("com.diffplug.spotless") version "6.25.0"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

val lombokVersion = "1.18.+"
val mockitoVersion = "5.11.+"
val assertjVersion = "3.25.+"

dependencies {
    // Existing dependencies remain unchanged
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    api(libs.commons.math3)
    implementation(libs.guava)

// SLF4J API
    implementation("org.slf4j:slf4j-api:2.0.+")
// Logback implementation
    implementation("ch.qos.logback:logback-classic:1.4.+")
    // Lombok dependencies using the single version variable
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // Mockito dependencies
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")

    // AssertJ for fluent assertions
    testImplementation("org.assertj:assertj-core:$assertjVersion")
}

dependencyLocking {
    lockAllConfigurations()
}

// Add this to your existing build.gradle.kts
tasks.test {
    useJUnitPlatform()
}
// Spotless configuration with Google Java Format
spotless {
    java {
        // Use the Google Java format style
        googleJavaFormat()

        // Enforce specific import order
        importOrder("java", "javax", "org", "com", "")

        // Remove unused imports
        removeUnusedImports()
    }

    // Format Kotlin files too (optional)
    kotlin {
        ktlint()
    }

    // Format Gradle files
    kotlinGradle {
        ktlint()
    }
}
