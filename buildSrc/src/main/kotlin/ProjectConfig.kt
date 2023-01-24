import org.gradle.api.JavaVersion

object ProjectConfig {
    const val name = "PureSchedule"
    const val compileSdk = 33
    const val targetSdk = 33
    const val minSdk = 23
    const val applicationId = "tool.xfy9326.schedule"
    const val versionCode = 33
    const val versionName = "1.4.4"

    val javaVersion = JavaVersion.VERSION_11
}