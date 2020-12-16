package tool.xfy9326.schedule.beans

import androidx.room.*
import tool.xfy9326.schedule.db.DBConst
import tool.xfy9326.schedule.kt.isEven
import tool.xfy9326.schedule.kt.isOdd
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
    companion object {
        private const val WEEK_NUM_CONNECT_SYMBOL = ", "
        private const val WEEK_NUM_PERIOD_SYMBOL = "-"
    }

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

    private fun getSpecialWeekModeDescription(startIndex: Int): Pair<Boolean, String>? {
        // Index count from 0, but course num start from 1
        val oddWeekMode = startIndex.isEven()
        var hasError = false
        var hasFinish = false
        var finishNum = startIndex
        var j = startIndex
        while (j < weekNum.size) {
            if (oddWeekMode && j.isEven() || !oddWeekMode && j.isOdd()) {
                if (weekNum[j]) {
                    if (hasFinish) {
                        hasError = true
                        break
                    }
                } else if (!hasFinish) {
                    hasFinish = true
                    finishNum = j + 1
                }
            } else if (weekNum[j]) {
                hasError = true
                break
            }
            j++
        }
        if (!hasError) {
            if (!hasFinish) finishNum = j
            if (startIndex != finishNum - 1) {
                return oddWeekMode to "${startIndex + 1}$WEEK_NUM_PERIOD_SYMBOL$finishNum"
            }
        }
        return null
    }

    fun weekNumDescription(): WeekNumDescription {
        var specialWeekMode = true
        var oddWeekMode = true
        val content = buildString {
            var i = 0
            var j: Int
            while (i < weekNum.size) {
                if (weekNum[i]) {
                    if (specialWeekMode) {
                        val testResult = getSpecialWeekModeDescription(i)
                        if (testResult == null) {
                            specialWeekMode = false
                        } else {
                            oddWeekMode = testResult.first
                            append(testResult.second)
                            break
                        }
                    }

                    j = i + 1
                    while (j < weekNum.size) {
                        if (!weekNum[j]) break
                        j++
                    }
                    if (i == j - 1) {
                        append(i + 1)
                    } else {
                        append(i + 1)
                        append(WEEK_NUM_PERIOD_SYMBOL)
                        append(j)
                    }
                    if (j < weekNum.size - 1) append(WEEK_NUM_CONNECT_SYMBOL)
                    i = j
                } else {
                    i++
                }
            }
        }
        return if (specialWeekMode) {
            if (oddWeekMode) {
                WeekNumDescription(content, WeekMode.ODD_WEEKS_ONLY)
            } else {
                WeekNumDescription(content, WeekMode.EVEN_WEEKS_ONLY)
            }
        } else {
            WeekNumDescription(content, WeekMode.ANY_WEEKS)
        }
    }
}