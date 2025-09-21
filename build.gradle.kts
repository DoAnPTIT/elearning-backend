plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.doanptit"
version = "0.0.1-SNAPSHOT"
description = "Backend system"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	//fly way
	implementation("org.flywaydb:flyway-core:11.10.0")
	implementation("org.flywaydb:flyway-database-postgresql:11.10.0")

	// Swagger / OpenAPI (SpringDoc)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	// Jackson YAML (bắt buộc cho SpringDoc)
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")


	//lombok
	compileOnly("org.projectlombok:lombok:1.18.34")
	annotationProcessor("org.projectlombok:lombok:1.18.34")
	testCompileOnly("org.projectlombok:lombok:1.18.34")
	testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
