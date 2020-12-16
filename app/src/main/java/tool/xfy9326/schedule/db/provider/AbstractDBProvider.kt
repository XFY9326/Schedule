package tool.xfy9326.schedule.db.provider

import androidx.room.Room
import androidx.room.RoomDatabase
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.kt.getSuperGenericTypeClass

abstract class AbstractDBProvider<T : RoomDatabase> {
    protected abstract val name: String

    val db: T by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        onBuildDB(Room.databaseBuilder(App.instance, this::class.getSuperGenericTypeClass(0), name))
    }

    protected open fun onBuildDB(builder: RoomDatabase.Builder<T>) = builder.build()
}