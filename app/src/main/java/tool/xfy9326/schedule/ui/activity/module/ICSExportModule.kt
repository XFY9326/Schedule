package tool.xfy9326.schedule.ui.activity.module

import androidx.activity.result.contract.ActivityResultContracts
import io.github.xfy9326.atools.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.ui.activity.ScheduleActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.ics.ScheduleICSHelper
import tool.xfy9326.schedule.utils.view.DialogUtils

class ICSExportModule(activity: ScheduleActivity) :
    AbstractViewModelActivityModule<ScheduleViewModel, ActivityScheduleBinding, ScheduleActivity>(activity) {
    private val exportICSFile = activity.registerForActivityResult(ActivityResultContracts.CreateDocument(MIMEConst.MIME_CALENDAR)) {
        if (it != null) {
            requireViewModel().exportICS(it)
        } else {
            requireViewModel().waitExportScheduleId.consume()
            requireViewBinding().layoutSchedule.showSnackBar(R.string.output_file_cancel)
        }
    }

    override fun onInit() {
        requireViewModel().selectScheduleForExportingICS.observeEvent(requireActivity()) {
            DialogUtils.showScheduleSelectDialog(requireActivity(), R.string.export_to_ics, it) { name, id ->
                requireViewModel().waitExportScheduleId.write(id)
                exportICSFile.launch(ScheduleICSHelper.createICSFileName(requireActivity(), name))
            }
        }
        requireViewModel().iceExportStatus.observeEvent(requireActivity()) {
            requireViewBinding().layoutSchedule.showSnackBar(if (it) R.string.output_file_success else R.string.output_file_failed)
        }
    }

    fun requestExport() {
        requireViewModel().selectScheduleForExportingICS()
    }
}