package tool.xfy9326.schedule.tools.ui

import android.content.ClipData
import android.net.Uri

fun ClipData.getItemUris(): List<Uri> {
    if (itemCount == 0) return emptyList()
    return buildList(itemCount) {
        for (i in 0 until itemCount) {
            getItemAt(i)?.uri?.let(::add)
        }
    }
}
