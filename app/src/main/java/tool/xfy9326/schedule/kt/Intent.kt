@file:Suppress("unused")

package tool.xfy9326.schedule.kt

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import io.github.xfy9326.atools.base.castNullable
import java.io.Serializable

inline fun <reified T : Serializable> Intent.getSerializableExtraCompat(name: String?): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getSerializableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializableExtra(name).castNullable<T>()
    }
}

inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String?): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(name)
    }
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayExtraCompat(name: String?): Array<out T>? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getParcelableArrayExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayExtra(name).castNullable()
    }
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(name: String?): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getParcelableArrayListExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayListExtra(name)
    }
}
