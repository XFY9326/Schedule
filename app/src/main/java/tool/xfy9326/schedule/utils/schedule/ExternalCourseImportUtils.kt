package tool.xfy9326.schedule.utils.schedule

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.xfy9326.atools.base.asList
import io.github.xfy9326.atools.ui.getItemUris
import io.github.xfy9326.atools.ui.showGlobalToast
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.beans.ExternalCourseImportData
import tool.xfy9326.schedule.ui.vm.ExternalCourseImportViewModel

object ExternalCourseImportUtils {
    private const val ACTION_EXTERNAL_COURSE_IMPORT = "${BuildConfig.BASE_APPLICATION_ID}.action.EXTERNAL_COURSE_IMPORT"
    private const val ACTION_JSON_COURSE_IMPORT = "${BuildConfig.BASE_APPLICATION_ID}.action.JSON_COURSE_IMPORT"
    private const val EXTRA_PROCESSOR_NAME = "PROCESSOR_NAME"
    private const val EXTRA_PROCESSOR_EXTRA_DATA = "PROCESSOR_EXTRA_DATA"
    private const val EXTRA_COMBINE_COURSE = "COMBINE_COURSE"
    private const val EXTRA_COMBINE_COURSE_TIME = "COMBINE_COURSE_TIME"
    private const val EXTRA_COMBINE_COURSE_TEACHER = "COMBINE_COURSE_TEACHER"
    private const val EXTRA_COMBINE_COURSE_TIME_LOCATION = "COMBINE_COURSE_TIME_LOCATION"

    private fun parseReceivedIntent(intent: Intent): ExternalCourseImportData.Origin? {
        if (intent.action == ACTION_EXTERNAL_COURSE_IMPORT) {
            val fileUri = getReceivedUriList(intent) ?: return null
            val processorName = intent.getStringExtra(EXTRA_PROCESSOR_NAME) ?: return null
            val processorExtraData = intent.getBundleExtra(EXTRA_PROCESSOR_EXTRA_DATA)
            return ExternalCourseImportData.Origin.External(fileUri, processorName, processorExtraData)
        } else if (intent.action == ACTION_JSON_COURSE_IMPORT) {
            val fileUri = intent.data ?: return null
            val combineCourse = intent.getBooleanExtra(EXTRA_COMBINE_COURSE, false)
            val combineCourseTime = intent.getBooleanExtra(EXTRA_COMBINE_COURSE_TIME, false)
            val combineCourseTeacher = intent.getBooleanExtra(EXTRA_COMBINE_COURSE_TEACHER, false)
            val combineCourseTimeLocation = intent.getBooleanExtra(EXTRA_COMBINE_COURSE_TIME_LOCATION, false)
            return ExternalCourseImportData.Origin.JSON(
                fileUriList = fileUri.asList(),
                combineCourse = combineCourse,
                combineCourseTime = combineCourseTime,
                combineCourseTeacher = combineCourseTeacher,
                combineCourseTimeLocation = combineCourseTimeLocation
            )
        }
        return null
    }

    private fun getReceivedUriList(intent: Intent): List<Uri>? =
        (intent.data?.asList() ?: intent.clipData?.getItemUris())?.takeIf { it.isNotEmpty() }

    fun prepareRunningEnvironment(activity: ComponentActivity): ExternalCourseImportViewModel? {
        val parseResult = parseReceivedIntent(activity.intent)
        return if (parseResult == null) {
            showGlobalToast(R.string.external_course_import_params_incomplete, lengthLong = true)
            null
        } else {
            try {
                return ViewModelProvider(activity, object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return super.create(modelClass).also {
                            if (it is ExternalCourseImportViewModel) {
                                it.importParams = parseResult
                            }
                        }
                    }
                })[ExternalCourseImportViewModel::class.java]
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                showGlobalToast(R.string.external_course_processor_name_error, lengthLong = true)
            } catch (e: Exception) {
                e.printStackTrace()
                showGlobalToast(R.string.external_course_init_error, lengthLong = true)
            }
            null
        }
    }
}