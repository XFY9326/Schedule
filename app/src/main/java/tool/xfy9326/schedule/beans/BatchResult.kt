package tool.xfy9326.schedule.beans

import java.io.Serializable

data class BatchResult(
    val success: Boolean,
    val total: Int = 0,
    val failedAmount: Int = 0,
) : Serializable