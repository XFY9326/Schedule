package tool.xfy9326.schedule.json.upgrade

import kotlinx.serialization.Serializable

@Serializable
data class UpdateIndex(
    val version: Int,
    val forceUpdate: Boolean,
)
