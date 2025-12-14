plugins {
	java
	id("org.springframework.boot") version "3.4.0"
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
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencyManagement {
    imports {
        // 2. Dùng bản M4 (Milestone 4) - Bản này rất ổn định với Boot 3.4.x
        mavenBom("org.springframework.ai:spring-ai-bom:1.0.0-M4")
    }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // --- SPRING AI
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    implementation("org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter")
    implementation("org.springframework.ai:spring-ai-transformers-spring-boot-starter")

    //fly way
	implementation("org.flywaydb:flyway-core:11.10.0")
	implementation("org.flywaydb:flyway-database-postgresql:11.10.0")

	// Swagger / OpenAPI (SpringDoc)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	// Jackson YAML (SpringDoc)
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")


	//spring security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// Validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

	//lombok
	compileOnly("org.projectlombok:lombok:1.18.34")
	annotationProcessor("org.projectlombok:lombok:1.18.34")
	testCompileOnly("org.projectlombok:lombok:1.18.34")
	testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

	// JWT (JSON Web Token)
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// AWS SDK v2 for DynamoDB
	val awsSdkVersion = "2.26.24"
    implementation("software.amazon.awssdk:s3:${awsSdkVersion}")
	implementation("software.amazon.awssdk:dynamodb:$awsSdkVersion")
	implementation("software.amazon.awssdk:url-connection-client:$awsSdkVersion")

	//mapstruct
	val mapstructVersion = "1.5.5.Final"
	implementation("org.mapstruct:mapstruct:$mapstructVersion")
	annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

	// Test dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")

    // Email
    implementation("org.springframework.boot:spring-boot-starter-mail")

	//  Thư viện để đọc và ghi file Microsoft Excel
	implementation("org.apache.poi:poi:5.2.5")
	implementation("org.apache.poi:poi-ooxml:5.2.5")

    // Thumbnailator (dùng để resize ảnh về chuẩn 256x256)
    implementation("net.coobird:thumbnailator:0.4.20")

	implementation("org.springframework.boot:spring-boot-starter-websocket")

}

tasks.withType<Test> {
	useJUnitPlatform()
}
