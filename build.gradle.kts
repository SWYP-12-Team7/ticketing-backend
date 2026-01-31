import org.gradle.api.Project.DEFAULT_VERSION
import org.springframework.boot.gradle.tasks.bundling.BootJar

// setting.gradle.kts에서 명시한 pluginManagement.repositories의 플러그인들에서 주입함
plugins {
    java // root에서 전체 빌드할 때 필요함
    id("org.springframework.boot") apply false // root에는 부트가 필요없어서 false 처리
    id("io.spring.dependency-management") // root에서 버전 공유하기 위해 필요함
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

fun getGitHash(): String {
    return runCatching {
        providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }.standardOutput.asText.get().trim()
    }.getOrElse { "init" }
}

allprojects {
    val projectGroup: String by project
    group = projectGroup
    version = if (version == DEFAULT_VERSION) getGitHash() else version

    repositories {
        mavenCentral() // 의존성 위치 명시
    }
}


subprojects {
    // 실제 소스가 있는 프로젝트만 필터링
    if (childProjects.isNotEmpty()) return@subprojects

    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "jacoco") // 테스트 커버리지

    dependencies {
        // Spring
        runtimeOnly("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter")

        // Serialize
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

        // Lombok
        implementation("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        // Test
        testRuntimeOnly("com.mysql:mysql-connector-j") // testcontainers:mysql이 jdbc 사용함
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("org.mockito:mockito-core:${project.properties["mockitoVersion"]}") // mocking용
        testImplementation("org.instancio:instancio-junit:${project.properties["instancioJUnitVersion"]}") // 객체 필드에 아무 값이나 채워줌

        // Testcontainers
        testImplementation("org.springframework.boot:spring-boot-testcontainers")
        testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    }

    // default: 모든 모듈을 라이브러리로 취금
    tasks.withType(Jar::class) { enabled = true }
    tasks.withType(BootJar::class) { enabled = false }

    // apps에 만든 모듈만 실행가능하도록
    configure(allprojects.filter { it.parent?.name.equals("apps") }) {
        tasks.withType(Jar::class) { enabled = false }
        tasks.withType(BootJar::class) { enabled = true }
    }

    tasks.test {
        enabled = !project.hasProperty("skipTests")      // -PskipTests 옵션으로 테스트 스킵 가능
        maxParallelForks = 1                              // 테스트 병렬 실행 안 함 (순차 실행)
        useJUnitPlatform()                                // JUnit 5 사용
        systemProperty("user.timezone", "Asia/Seoul")    // 테스트 시 타임존 설정
        systemProperty("spring.profiles.active", "test") // test 프로파일 활성화
        jvmArgs("-Xshare:off")                           // 클래스 데이터 공유 비활성화 (Testcontainers 호환성)
    }

    // jacoco 커버리지 세팅 (xml만 허용)
    tasks.withType<JacocoReport> {
        mustRunAfter("test")
        executionData(fileTree(layout.buildDirectory.asFile).include("jacoco/*.exec"))
        reports {
            xml.required = true
            csv.required = false
            html.required = false
        }
        afterEvaluate {
            classDirectories.setFrom(
                files(
                    classDirectories.files.map {
                        fileTree(it)
                    },
                ),
            )
        }
    }
}

tasks.matching { it.project == rootProject }.configureEach {
    enabled = false
}
