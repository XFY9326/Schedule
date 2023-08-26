package tool.xfy9326.schedule.content.beans

import tool.xfy9326.schedule.content.utils.CourseParseResult
import java.io.Serializable

class JSParams(
    val uuid: String,
    val jsType: String,
    val initUrl: String,
    val requireNetwork: Boolean,
    val parseParams: CourseParseResult.Params,
    val asyncEnvironment: Boolean
) : Serializable
