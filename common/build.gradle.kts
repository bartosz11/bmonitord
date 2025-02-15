plugins {
    java
    `java-library`
    id("io.spring.dependency-management") version "1.1.7" // ✅ Keep this for BOM management
}

group = "one.bartosz"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.2") // ✅ Add Spring Boot BOM manually
    }
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-r2dbc") // ✅ Now it gets the version from the BOM
    api("org.springframework:spring-jdbc")
    api("org.springframework:spring-context")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
