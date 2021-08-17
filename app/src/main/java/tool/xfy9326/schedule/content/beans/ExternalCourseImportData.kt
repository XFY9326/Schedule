package tool.xfy9326.schedule.content.beans

import android.net.Uri
import android.os.Bundle

class ExternalCourseImportData(
    val fileContent: String,
    val bundle: Bundle?,
) {
    class Origin(
        val fileUri: Uri,
        val processorName: String,
        val processorExtraData: Bundle?,
    )
}