package tool.xfy9326.schedule.content.beans

import android.net.Uri
import android.os.Bundle

class ExternalCourseImportData(
    val fileContent: String,
    val bundle: Bundle?,
) {
    sealed interface Origin {
        val fileUri: Uri

        class External(
            override val fileUri: Uri,
            val processorName: String,
            val processorExtraData: Bundle?,
        ) : Origin

        class JSON(
            override val fileUri: Uri,
            val combineCourse: Boolean,
            val combineCourseTime: Boolean,
        ) : Origin
    }
}