@file:Suppress("unused")

package tool.xfy9326.schedule.kt

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import io.github.xfy9326.atools.base.castNullable
import java.io.Serializable

inline fun <reified T : Serializable> Bundle.getSerializableCompat(name: String?): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getSerializable(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializable(name).castNullable<T>()
    }
}

inline fun <reified T : Parcelable> Bundle.getParcelableCompat(name: String?): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getParcelable(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(name)
    }
}

inline fun <reified T : Parcelable> Bundle.getParcelableArrayCompat(name: String?): Array<out T>? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getParcelableArray(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArray(name).castNullable()
    }
}

inline fun <reified T : Parcelable> Bundle.getSparseParcelableArrayCompat(name: String?): SparseArray<T>? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getSparseParcelableArray(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSparseParcelableArray(name)
    }
}

inline fun <reified T : Parcelable> Bundle.getParcelableArrayListCompat(name: String?): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getParcelableArrayList(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayList(name)
    }
}
