package tool.xfy9326.schedule.tools.ui

import androidx.fragment.app.Fragment
import tool.xfy9326.schedule.tools.base.tryCast

fun <T> Fragment.requireOwner() = (parentFragment ?: requireActivity()).tryCast<T>()