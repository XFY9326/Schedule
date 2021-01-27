package tool.xfy9326.schedule.json.upgrade

import kotlinx.serialization.Serializable

@Serializable
data class DownloadSource(
    val sourceName: String,
    val url: String,
    val isDirectLink: Boolean,
) : java.io.Serializable
