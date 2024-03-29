package tool.xfy9326.schedule.beans

import androidx.appcompat.app.AppCompatDelegate

enum class NightMode {
    FOLLOW_SYSTEM,
    ENABLED,
    DISABLED;

    companion object {
        val NightMode.modeInt
            get() = when (this) {
                FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ENABLED -> AppCompatDelegate.MODE_NIGHT_YES
                DISABLED -> AppCompatDelegate.MODE_NIGHT_NO
            }
    }
}