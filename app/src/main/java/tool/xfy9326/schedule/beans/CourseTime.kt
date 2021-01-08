package tool.xfy9326.schedule.beans

import androidx.room.*
import tool.xfy9326.schedule.db.DBConst
import java.io.Serializable
import kotlin.math.min

@Entity(
    tableName = DBConst.TABLE_COURSE_TIME,
    foreignKeys = [ForeignKey(
        entity = Course::class,
        parentColumns = [DBConst.COLUMN_COURSE_ID],
        childColumns = [DBConst.COLUMN_COURSE_ID],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(
        value = [DBConst.COLUMN_COURSE_ID, DBConst.COLUMN_WEEK_DAY, DBConst.COLUMN_CLASS_START_TIME, DBConst.COLUMN_CLASS_DURATION, DBConst.COLUMN_WEEK_NUM],
        unique = true
    )]
)
data class CourseTime(
    @ColumnInfo(name = DBConst.COLUMN_TIME_ID)
    @PrimaryKey(autoGenerate = true)
    var timeId: Long,
    @ColumnInfo(name = DBConst.COLUMN_COURSE_ID, index = true)
    var courseId: Long,
    @ColumnInfo(name = DBConst.COLUMN_WEEK_NUM)
    var weekNum: BooleanArray,
    @Embedded
    var classTime: ClassTime,
    var location: String?,
) : Serializable {
    val weekNumPattern: WeekNumPattern
        get() = WeekNumPattern(weekNum)

    constructor(weekNum: BooleanArray, weekDay: WeekDay, classStartTime: Int, classDuration: Int, location: String? = null) :
            this(DBConst.DEFAULT_ID, DBConst.DEFAULT_ID, weekNum, ClassTime(weekDay, classStartTime, classDuration), location)

    fun hasThisWeekCourse(num: Int): Boolean {
        val index = num - 1
        return if (index in weekNum.indices) {
            weekNum[index]
        } else {
            false
        }
    }

    operator fun compareTo(courseTime: CourseTime): Int {
        for (i in 0 until min(weekNum.size, courseTime.weekNum.size)) {
            if (weekNum[i] && !courseTime.weekNum[i]) {
                return 1
            } else if (!weekNum[i] && courseTime.weekNum[i]) {
                return -1
            }
        }
        return classTime.compareTo(courseTime.classTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseTime

        if (timeId != other.timeId) return false
        if (courseId != other.courseId) return false
        if (!weekNum.contentEquals(other.weekNum)) return false
        if (classTime != other.classTime) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeId.hashCode()
        result = 31 * result + courseId.hashCode()
        result = 31 * result + weekNum.contentHashCode()
        result = 31 * result + classTime.hashCode()
        result = 31 * result + (location?.hashCode() ?: 0)
        return result
    }
}