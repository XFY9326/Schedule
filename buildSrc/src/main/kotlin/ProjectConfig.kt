import org.gradle.api.JavaVersion

object ProjectConfig {
    const val name = "PureSchedule"
    const val compileSdk = 32
    const val targetSdk = 32
    const val minSdk = 23
    const val applicationId = "tool.xfy9326.schedule"
    const val versionCode = 29
    const val versionName = "1.4.0"

    val javaVersion = JavaVersion.VERSION_11
}