package tool.xfy9326.schedule.beans

import androidx.appcompat.app.AppCompatDelegate

enum class NightMode(val modeInt: Int) {
    FOLLOW_SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    ENABLED(AppCompatDelegate.MODE_NIGHT_YES),
    DISABLED(AppCompatDelegate.MODE_NIGHT_NO)
}