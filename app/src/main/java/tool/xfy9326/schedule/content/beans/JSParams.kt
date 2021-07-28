package tool.xfy9326.schedule.content.beans

import java.io.Serializable

class JSParams(
    val uuid: String,
    val jsType: String,
    val initUrl: String,
    val requireNetwork: Boolean,
    val combineCourse: Boolean,
    val combineCourseTime: Boolean,
) : Serializable