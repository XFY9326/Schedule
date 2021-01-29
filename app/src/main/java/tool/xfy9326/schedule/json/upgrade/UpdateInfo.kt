package tool.xfy9326.schedule.json.upgrade

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val forceUpdate: Boolean,
    val changeLog: String,
    val downloadSource: List<DownloadSource>,
) : java.io.Serializable
