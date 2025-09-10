plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.asyncsite"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()

	maven {
		url = uri("https://maven.pkg.github.com/AsyncSite/core-platform")
		credentials {
			username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
			password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
		}
	}
}

extra["springCloudVersion"] = "2024.0.1"

dependencies {
	// Core Platform Common
	implementation("com.asyncsite.coreplatform:common:1.0.0-SNAPSHOT")

	// Logging
	implementation("net.logstash.logback:logstash-logback-encoder:8.0")

	// Spring Boot Starters
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	
	// Spring Cloud
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	
	// Kafka
	implementation("org.springframework.kafka:spring-kafka")
	
	// OAuth2 Client for Slack
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	
	// OpenAPI/Swagger Documentation
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0")
	implementation("org.springdoc:springdoc-openapi-starter-common:2.8.0")
	
	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	
	// Database
	runtimeOnly("com.h2database:h2")
	runtimeOnly("com.mysql:mysql-connector-j")
	
	// Test Dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("spring.profiles.active", "test")
}

// Apply Docker tasks
apply(from = "docker-tasks.gradle.kts")