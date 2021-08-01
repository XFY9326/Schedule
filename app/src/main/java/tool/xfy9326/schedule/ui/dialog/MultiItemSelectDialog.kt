package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import lib.xfy9326.android.kit.requireOwner

class MultiItemSelectDialog : AppCompatDialogFragment() {
    companion object {
        private const val EXTRA_TAG = "EXTRA_TAG"
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_SHOW_ARR = "EXTRA_SHOW_ARR"
        private const val EXTRA_ID_ARR = "EXTRA_ID_ARR"
        private const val EXTRA_SELECTED_ARR = "EXTRA_SELECTED_ARR"

        fun showDialog(
            fragmentManager: FragmentManager,
            tag: String? = null,
            title: String,
            showArr: Array<String>,
            idArr: LongArray,
            selectedArr: BooleanArray,
        ) {
            require(showArr.size == selectedArr.size && showArr.size == idArr.size)
            MultiItemSelectDialog().apply {
                arguments = bundleOf(
                    EXTRA_TAG to tag,
                    EXTRA_TITLE to title,
                    EXTRA_SHOW_ARR to showArr,
                    EXTRA_ID_ARR to idArr,
                    EXTRA_SELECTED_ARR to selectedArr
                )
            }.show(fragmentManager, tag)
        }
    }

    private lateinit var selectedArr: BooleanArray
    private lateinit var showArr: Array<String>
    private lateinit var idArr: LongArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedArr = savedInstanceState?.getBooleanArray(EXTRA_SELECTED_ARR) ?: requireArguments().getBooleanArray(EXTRA_SELECTED_ARR) ?: BooleanArray(0)
        showArr = requireArguments().getStringArray(EXTRA_SHOW_ARR) ?: emptyArray()
        idArr = requireArguments().getLongArray(EXTRA_ID_ARR) ?: LongArray(0)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(requireArguments().getString(EXTRA_TITLE))
            setMultiChoiceItems(showArr, selectedArr) { _, which, isChecked ->
                selectedArr[which] = isChecked
            }
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                requireOwner<OnMultiItemSelectedListener>()?.onMultiItemSelected(requireArguments().getString(EXTRA_TAG), idArr, selectedArr)
            }
        }.create()

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBooleanArray(EXTRA_SELECTED_ARR, selectedArr)
        super.onSaveInstanceState(outState)
    }

    interface OnMultiItemSelectedListener {
        fun onMultiItemSelected(tag: String?, idArr: LongArray, selectedArr: BooleanArray)
    }
}