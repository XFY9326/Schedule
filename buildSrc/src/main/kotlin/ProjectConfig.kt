import org.gradle.api.JavaVersion

object ProjectConfig {
    const val name = "PureSchedule"
    const val compileSdk = 31
    const val targetSdk = 31
    const val minSdk = 23
    const val applicationId = "tool.xfy9326.schedule"
    const val versionCode = 27
    const val versionName = "1.3.8"

    val javaVersion = JavaVersion.VERSION_11
}