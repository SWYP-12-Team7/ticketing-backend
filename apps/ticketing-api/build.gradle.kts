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

    // Spring AI - Google GenAI (Gemini)
    implementation("org.springframework.ai:spring-ai-starter-model-google-genai:$springAiVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
}
