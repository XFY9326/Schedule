package tool.xfy9326.schedule.utils

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import lib.xfy9326.android.kit.showGlobalToast
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

    private fun parseReceivedIntent(intent: Intent): ExternalCourseImportData.Origin? {
        if (intent.action == ACTION_EXTERNAL_COURSE_IMPORT) {
            val fileUri = intent.data ?: return null
            val processorName = intent.getStringExtra(EXTRA_PROCESSOR_NAME) ?: return null
            val processorExtraData = intent.getBundleExtra(EXTRA_PROCESSOR_EXTRA_DATA)
            return ExternalCourseImportData.Origin.External(fileUri, processorName, processorExtraData)
        } else if (intent.action == ACTION_JSON_COURSE_IMPORT) {
            val fileUri = intent.data ?: return null
            val combineCourse = intent.getBooleanExtra(EXTRA_COMBINE_COURSE, false)
            val combineCourseTime = intent.getBooleanExtra(EXTRA_COMBINE_COURSE_TIME, false)
            return ExternalCourseImportData.Origin.JSON(fileUri, combineCourse, combineCourseTime)
        }
        return null
    }

    fun prepareRunningEnvironment(activity: ComponentActivity): ExternalCourseImportViewModel? {
        val parseResult = parseReceivedIntent(activity.intent)
        return if (parseResult == null) {
            showGlobalToast(R.string.external_course_import_params_incomplete, showLong = true)
            null
        } else {
            try {
                return ViewModelProvider(activity, object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return super.create(modelClass).also {
                            if (it is ExternalCourseImportViewModel) {
                                it.importParams = parseResult
                            }
                        }
                    }
                })[ExternalCourseImportViewModel::class.java]
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                showGlobalToast(R.string.external_course_processor_name_error, showLong = true)
            } catch (e: Exception) {
                e.printStackTrace()
                showGlobalToast(R.string.external_course_init_error, showLong = true)
            }
            null
        }
    }
}