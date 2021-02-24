package tool.xfy9326.schedule.content.base

import java.io.Serializable

abstract class AbstractCourseProvider<P : Serializable> {
    var params: P? = null
}