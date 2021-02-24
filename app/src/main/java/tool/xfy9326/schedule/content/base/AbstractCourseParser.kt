package tool.xfy9326.schedule.content.base

import java.io.Serializable

abstract class AbstractCourseParser<P : Serializable> {
    var params: P? = null
}