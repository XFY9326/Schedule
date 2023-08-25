package tool.xfy9326.schedule.content.beans

import android.net.Uri
import android.os.Bundle
import tool.xfy9326.schedule.content.utils.CourseParseResult

class ExternalCourseImportData(
    val fileContentList: List<String>,
    val bundle: Bundle?,
) {
    sealed interface Origin {
        val fileUriList: List<Uri>

        class External(
            override val fileUriList: List<Uri>,
            val processorName: String,
            val processorExtraData: Bundle?,
        ) : Origin

        class JSON(
            override val fileUriList: List<Uri>,
            combineCourse: Boolean,
            combineCourseTime: Boolean,
            combineCourseTeacher: Boolean,
            combineCourseTimeLocation: Boolean,
        ) : Origin {
            val parseParams = CourseParseResult.Params(
                combineCourse = combineCourse,
                combineCourseTime = combineCourseTime,
                combineCourseTeacher = combineCourseTeacher,
                combineCourseTimeLocation = combineCourseTimeLocation
            )
        }
    }
}