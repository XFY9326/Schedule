package tool.xfy9326.schedule.beans

data class TimePeriod(val start: Int, val end: Int) {
    constructor(start: Int) : this(start, start)

    val length = end - start + 1
}