package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.WebPageContent
import java.io.Serializable

abstract class WebCourseParser<P : Serializable> : AbstractSimpleCourseParser<P, WebPageContent>()