package tool.xfy9326.schedule.db.provider

import androidx.room.Room
import androidx.room.RoomDatabase
import tool.xfy9326.schedule.kt.AppInstance
import kotlin.reflect.KClass

abstract class AbstractDBProvider<T : RoomDatabase>(private val dbClass: KClass<T>) {
    protected abstract val name: String

    val db: T by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        onBuildDB(Room.databaseBuilder(AppInstance, dbClass.java, name))
    }

    protected open fun onBuildDB(builder: RoomDatabase.Builder<T>) = builder.build()
}