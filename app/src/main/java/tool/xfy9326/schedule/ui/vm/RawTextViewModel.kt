package tool.xfy9326.schedule.ui.vm

import androidx.annotation.RawRes
import androidx.lifecycle.viewModelScope
import io.github.xfy9326.atools.io.file.rawResFile
import io.github.xfy9326.atools.io.okio.readTextAsync
import io.github.xfy9326.atools.livedata.MutableEventLiveData
import io.github.xfy9326.atools.livedata.postEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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