@file:Suppress("unused")

package tool.xfy9326.schedule.tools.io.okio

import okio.BufferedSink
import okio.BufferedSource
import java.io.IOException

private class LineIterator(private val source: BufferedSource) : Iterator<String> {
    private var nextValue: String? = null
    private var done = false

    override fun hasNext(): Boolean {
        if (nextValue == null && !done) {
            nextValue = source.readUtf8Line()
            if (nextValue == null) done = true
        }
        return nextValue != null
    }

    override fun next(): String {
        if (!hasNext()) throw NoSuchElementException()
        val answer = nextValue
        nextValue = null
        return answer!!
    }
}

@Throws(IOException::class)
fun BufferedSource.lineIterator(): Iterator<String> = LineIterator(this)

@Throws(IOException::class)
fun BufferedSource.forEachLine(block: (String) -> Unit): Unit =
    lineIterator().forEach(block)

@Throws(IOException::class)
fun BufferedSource.readAllLines(): List<String> =
    lineIterator().asSequence().toList()

@Throws(IOException::class)
fun BufferedSink.writeLines(iterator: Iterator<String>, lineSeparator: String = System.lineSeparator()) {
    while (iterator.hasNext()) {
        writeUtf8(iterator.next())
        if (iterator.hasNext()) {
            writeUtf8(lineSeparator)
        }
    }
}

@Throws(IOException::class)
fun BufferedSink.writeLines(array: Array<String>, lineSeparator: String = System.lineSeparator()) =
    writeLines(array.iterator(), lineSeparator)
