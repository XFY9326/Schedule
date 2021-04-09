package tool.xfy9326.schedule.json.upgrade

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DownloadSource(
    val sourceName: String,
    val url: String,
    val isDirectLink: Boolean,
) : Parcelable
