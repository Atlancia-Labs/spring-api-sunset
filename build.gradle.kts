import com.vanniktech.maven.publish.SonatypeHost

plugins {
    java
    signing
    id("com.vanniktech.maven.publish") version "0.30.0"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.atlancia-labs"
version = findProperty("version")?.toString()?.takeIf { it != "unspecified" } ?: "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    compileOnly("io.micrometer:micrometer-core")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.micrometer:micrometer-core")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}

tasks.withType<GenerateModuleMetadata> {
    suppressedValidationErrors.add("enforced-platform")
    suppressedValidationErrors.add("dependencies-without-versions")
}

signing {
    val signingKeyId = findProperty("signingInMemoryKeyId") as String?
    val signingKey = findProperty("signingInMemoryKey") as String?
    val signingPassword = findProperty("signingInMemoryKeyPassword") as String?
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    }
    isRequired = signingKey != null
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("Spring API Sunset")
        description.set("Spring Boot starter for managing API deprecation lifecycle with RFC 8594/9745 headers")
        url.set("https://github.com/Atlancia-Labs/spring-api-sunset")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("atlancia")
                name.set("Atlancia Labs")
                url.set("https://github.com/Atlancia-Labs")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/Atlancia-Labs/spring-api-sunset.git")
            developerConnection.set("scm:git:ssh://github.com/Atlancia-Labs/spring-api-sunset.git")
            url.set("https://github.com/Atlancia-Labs/spring-api-sunset")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Atlancia-Labs/spring-api-sunset")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}
