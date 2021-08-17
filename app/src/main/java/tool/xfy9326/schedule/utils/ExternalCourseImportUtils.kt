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
    private const val EXTRA_PROCESSOR_NAME = "PROCESSOR_NAME"
    private const val EXTRA_PROCESSOR_EXTRA_DATA = "PROCESSOR_EXTRA_DATA"

    private fun parseReceivedIntent(intent: Intent): ExternalCourseImportData.Origin? {
        if (intent.action == ACTION_EXTERNAL_COURSE_IMPORT) {
            val fileUri = intent.data ?: return null
            val processorName = intent.getStringExtra(EXTRA_PROCESSOR_NAME) ?: return null
            val processorExtraData = intent.getBundleExtra(EXTRA_PROCESSOR_EXTRA_DATA)
            return ExternalCourseImportData.Origin(fileUri, processorName, processorExtraData)
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
                        return try {
                            modelClass.getConstructor(ExternalCourseImportData.Origin::class.java).newInstance(parseResult)
                        } catch (e: NoSuchMethodException) {
                            modelClass.newInstance()
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