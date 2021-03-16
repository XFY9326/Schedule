package tool.xfy9326.schedule.json.upgrade

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val forceUpdate: Boolean,
    val changeLog: String,
    val downloadSource: List<DownloadSource>,
) : Parcelable
