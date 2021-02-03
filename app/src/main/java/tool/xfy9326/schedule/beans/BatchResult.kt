package tool.xfy9326.schedule.beans

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BatchResult(
    val success: Boolean,
    val total: Int = 0,
    val failedAmount: Int = 0,
) : Parcelable