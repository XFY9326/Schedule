package tool.xfy9326.schedule.content.adapters.parser

import io.github.xfy9326.atools.base.EMPTY
import io.github.xfy9326.atools.base.cast
import org.json.JSONObject
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.content.base.NetworkCourseParser
import tool.xfy9326.schedule.content.utils.CourseParseResult

class AHSTUCourseParser : NetworkCourseParser<Nothing>() {
    @Suppress("RegExpRedundantEscape")
    private val patternTeacher = "\\[\\{id:\\d+,name:\"(.*?)\",lab:(false|true)\\}\\];".toRegex()
    private val patternCourseInfo = "actTeacherName.join\\(','\\),\"(.*?)\",\"(.*?)\",\".*?\",\"(.*?)\",\"(.*?)\",.*?assistantName,\".*?\",\"(.*?)\"".toRegex()
    private val patternDay = "(\\d+)\\*[\\s]*unitCount[\\s]*\\+(\\d+)".toRegex()

    @Suppress("RegExpRedundantEscape")
    private val splitRegex = "var\\s*teachers\\s*=\\s*\\[.*?\\];".toRegex()

    override fun onParseScheduleTimes(importOption: Int, content: String?) =
        ScheduleTime.listOf(
            8, 0, 8, 45,
            8, 55, 9, 40,
            10, 0, 10, 45,
            10, 55, 11, 40,

            14, 0, 14, 45,
            14, 55, 15, 40,
            16, 0, 16, 45,
            16, 55, 17, 40,

            19, 0, 19, 45,
            19, 55, 20, 40,
            20, 50, 21, 35,
            21, 45, 22, 30
        )

    // 将在解析课程时调用
    // 参数：导入参数，待解析的HTML
    // 返回：课程解析结果
    override fun onParseCourses(importOption: Int, content: String?): CourseParseResult {
        if (content == null) return CourseParseResult.EMPTY

        val result = parse(content)
        val builder = CourseParseResult.Builder(result.size)

        for (jsonObjects in result.values) {
            val courseTimes = ArrayList<CourseTime>()
            for (jsonObject in jsonObjects) { // 同一classId的课程
                val tpHMap = jsonObject["time"].cast<HashMap<Int, IntArray>>()
                for (k: Int in (tpHMap).keys) { // 星期几  [第几节课,持续几节]
                    courseTimes.add(CourseTime(jsonObject["week"].cast(), WeekDay.of(k), tpHMap[k]!![0], tpHMap[k]!![1], jsonObject["classRoom"].toString()))
                }
            }
            builder.add(Course(jsonObjects[0]["className"].toString(), jsonObjects[0]["teacherName"].toString(), courseTimes.toList()))
        }

        return builder.build()
    }

    //xk
    private fun parseWeek(str: String): BooleanArray {
        //00000100011111100000000
        val tp = BooleanArray(str.length - 1)
        var i = 0
        while (i < tp.size) {
            tp[i] = str[i + 1] == '1'
            i++
        }
        return tp
    }

    private fun parse(courseJs: String): HashMap<String, ArrayList<JSONObject>> {
        val ret: HashMap<String, ArrayList<JSONObject>> = HashMap()//课程id
        val items = courseJs.split(splitRegex)
        var i = 1
        while (i < items.size) {
            val tp1 = parseItem(items[i])
            val classId: String = tp1["classId"].toString()
            if (ret.containsKey(classId)) {
                ret[classId]?.add(tp1)
            } else {
                ret[classId] = arrayListOf(tp1)
            }
            i++
        }
        return ret
    }

    private fun parseItem(item: String): JSONObject {
        val ret = JSONObject()
        // 匹配教师姓名
        val matchesTeacher = patternTeacher.find(item)!!
        ret.put("teacherName", matchesTeacher.groupValues[1]) // 教师姓名
        // 匹配课程信息
        val matchesInfo = patternCourseInfo.find(item)!!

        val tpName = if (matchesInfo.groupValues[5].isNotEmpty()) {
            "组${matchesInfo.groupValues[5]}"
        } else {
            EMPTY
        }
        ret.put("classId", matchesInfo.groupValues[1])
        ret.put("className", parseClassname(matchesInfo.groupValues[2]))
        ret.put("classRoom", "${matchesInfo.groupValues[3]} $tpName")
        ret.put("week", parseWeek(matchesInfo.groupValues[4]))

        val tp = HashMap<Int, IntArray>() // 星期几  [第几节课,持续几节]
        val matcherTime = patternDay.findAll(item)
        for (timeMatchResult in matcherTime.iterator()) {
            val w = timeMatchResult.groupValues[1].toInt() + 1
            if (tp.containsKey(w)) {
                tp[w]!![1] = tp[w]!![1] + 1
            } else {
                tp[w] = intArrayOf(timeMatchResult.groupValues[2].toInt() + 1, 1)
            }
        }
        ret.put("time", tp)
        return ret
    }

    private fun parseClassname(className: String): String {
        var tpI = 0
        for (i in className.length - 1 downTo 0) {
            when (className[i]) {
                ')' -> tpI++
                '(' -> tpI--
                else -> continue
            }
            if (tpI == 0) {
                return className.substring(0, i)
            }
        }
        return ""
    }
}
