package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.databinding.DialogUpgradeBinding
import tool.xfy9326.schedule.json.upgrade.DownloadSource
import tool.xfy9326.schedule.json.upgrade.UpdateInfo
import tool.xfy9326.schedule.kt.APP_ID
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.kt.setWindowWidthPercent
import tool.xfy9326.schedule.kt.showShortToast
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.utils.DownloadUtils

class UpgradeDialog : DialogFragment() {
    companion object {
        const val UPDATE_INFO = "UPDATE_INFO"

        private val UPDATE_FRAGMENT_TAG = UpgradeDialog::class.simpleName
        private const val CONTENT_WIDTH_PERCENT = 0.9

        fun showDialog(fragmentManager: FragmentManager, updateInfo: UpdateInfo) {
            if (fragmentManager.findFragmentByTag(UPDATE_FRAGMENT_TAG) == null) {
                try {
                    UpgradeDialog().apply {
                        arguments = buildBundle {
                            putSerializable(UPDATE_INFO, updateInfo)
                        }
                    }.show(fragmentManager, UPDATE_FRAGMENT_TAG)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private lateinit var updateInfo: UpdateInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateInfo = requireArguments().getSerializable(UPDATE_INFO) as UpdateInfo
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext()).apply {
        setTitle(R.string.found_new_version)

        val binding = DialogUpgradeBinding.inflate(layoutInflater)

        binding.textViewUpgradeVersion.text = getString(
            R.string.update_version_info, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE,
            updateInfo.versionName, updateInfo.versionCode
        )
        binding.textViewUpgradeAttention.isVisible = updateInfo.forceUpdate
        binding.textViewUpgradeChangeLog.text = updateInfo.changeLog

        if (!updateInfo.forceUpdate) {
            setNeutralButton(R.string.ignore_update, null)
            setNegativeButton(android.R.string.cancel, null)
        }
        setPositiveButton(R.string.update_now, null)

        setView(binding.root)
    }.create().also { dialog ->
        dialog.setOnShowListener {
            setupUpdateDownloadSource(dialog.getButton(Dialog.BUTTON_POSITIVE))
            if (!updateInfo.forceUpdate) setupIgnoreUpdate(dialog.getButton(Dialog.BUTTON_NEUTRAL))
        }
    }

    private fun setupUpdateDownloadSource(downloadButton: Button) {
        downloadButton.setOnClickListener {
            when {
                updateInfo.downloadSource.size > 1 -> buildDownloadSourceMenu(it).show()
                updateInfo.downloadSource.isNotEmpty() -> {
                    val source = updateInfo.downloadSource.first()
                    downloadFile(source)
                }
                else -> showShortToast(R.string.no_update_source)
            }
        }
    }

    private fun buildDownloadSourceMenu(anchorView: View) =
        PopupMenu(requireContext(), anchorView).apply {
            for ((i, source) in updateInfo.downloadSource.withIndex()) {
                menu.add(Menu.NONE, Menu.NONE, i, source.sourceName)
            }
            gravity = Gravity.BOTTOM or Gravity.END

            setOnMenuItemClickListener {
                val source = updateInfo.downloadSource[it.order]
                downloadFile(source)
                return@setOnMenuItemClickListener true
            }
        }

    private fun downloadFile(downloadSource: DownloadSource) {
        lifecycleScope.launch {
            if (downloadSource.isDirectLink && !AppSettingsDataStore.useBrowserDownloadUpgradeFileFlow.first()) {
                val title = getString(R.string.app_name)
                val description = getString(R.string.downloading_update_description, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
                val fileName = "${APP_ID}_v${BuildConfig.VERSION_NAME}_${BuildConfig.VERSION_CODE}.apk"
                val downloadId =
                    DownloadUtils.requestDownloadFileDirectly(requireContext(), downloadSource.url, fileName, title, description, MIMEConst.MIME_APK)
                if (downloadId == null) {
                    showShortToast(R.string.directly_download_failed)
                    DownloadUtils.requestDownloadFileByBrowser(requireContext(), downloadSource.url)
                } else {
                    showShortToast(R.string.start_download_update)
                }
            } else {
                DownloadUtils.requestDownloadFileByBrowser(requireContext(), downloadSource.url)
            }
        }
    }

    private fun setupIgnoreUpdate(ignoreButton: Button) {
        ignoreButton.setOnClickListener {
            lifecycleScope.launch {
                AppDataStore.setIgnoreUpdateVersionCode(updateInfo.versionCode)
                dismissAllowingStateLoss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            setCancelable(!updateInfo.forceUpdate)
            setCanceledOnTouchOutside(!updateInfo.forceUpdate)
            setWindowWidthPercent(CONTENT_WIDTH_PERCENT)
        }
    }
}