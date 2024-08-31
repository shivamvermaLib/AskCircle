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
    }
}

rootProject.name = "AskCircle"
include(":app")
include(":data:user")
include(":data:widget")
include(":data:core")
include(":data:country")
include(":feature:home")
include(":feature:create")
include(":feature:common")
include(":data:analytics")
include(":domain:user")
include(":domain:widget")
include(":workmanager")
include(":domain:common")
include(":feature:splash")
include(":domain:country")
include(":data:category")
include(":domain:category")
include(":feature:admin")
include(":feature:profile")
include(":feature:imageview")
include(":feature:widgetdetails")
include(":feature:settings")
include(":feature:maintenancemode")
