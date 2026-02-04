repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

val springAiVersion = "1.1.1"

dependencies {
    implementation(project(":modules:jpa"))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-batch")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.testcontainers:testcontainers-mysql")
    testImplementation("org.awaitility:awaitility:4.2.0")
    
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // WebClient (카카오 API 호출용)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3")

    // Spring AI - Google GenAI (Gemini)
    implementation("org.springframework.ai:spring-ai-starter-model-google-genai:$springAiVersion")
}

