package tool.xfy9326.schedule.ui.vm

import androidx.annotation.RawRes
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.tools.io.file.rawResFile
import tool.xfy9326.schedule.tools.io.okio.readTextAsync
import tool.xfy9326.schedule.tools.livedata.MutableEventLiveData
import tool.xfy9326.schedule.tools.livedata.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class RawTextViewModel : AbstractViewModel() {
    val rawText = MutableEventLiveData<String>()

    fun loadRawText(@RawRes resId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = rawResFile(resId).readTextAsync().getOrThrow()
            rawText.postEvent(content)
        }
    }
}