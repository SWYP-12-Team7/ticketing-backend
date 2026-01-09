rootProject.name = "example"

include(
    ":apps:ticketing-api",
    ":modules:jpa"
)

pluginManagement {
    // `by settings`로 gradle.properties에 접근
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    repositories {
        // 정식버전만 쓰도록 플러그인 명시 (마일스톤, 스냅샷 사용못함)
        gradlePluginPortal()
    }

    resolutionStrategy {
        // 하위 모듈에서 버전 명시안하면 root에서 사용하는 버전으로 주입함
        eachPlugin {
            when (requested.id.id) {
                "org.springframework.boot" -> useVersion(springBootVersion)
                "io.spring.dependency-management" -> useVersion(springDependencyManagementVersion)
            }
        }
    }
}
