// settings.gradle.kts (Project: androidProject)

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // (★ 수정 ★) '새' '달력' '라이브러리' '설치'를 '위해' 'JitPack' '저장소' '주소'를 '추가'합니다.
        // ('url = uri(...)' '문법'을 '사용'해야 '합니다'.)
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "androidProject"
include(":app")