@file:Suppress("unused")

import org.gradle.api.JavaVersion
import tool.xfy9326.build.plugin.config.AndroidConfig
import tool.xfy9326.build.plugin.config.DependenciesConfig

const val ProjectName = "PureSchedule"

val PROJECT_JAVA_VERSION = JavaVersion.VERSION_11

typealias Android = AndroidConfig
typealias Dependencies = DependenciesConfig