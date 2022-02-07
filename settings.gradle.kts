pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":Annotation")
include(":AnnotationKSP")
include(":LiveDataTools")
include(":ToolKit")
include(":AndroidToolKit")
include(":AndroidToolKitIO")
include(":app")

rootProject.name = "Schedule"

