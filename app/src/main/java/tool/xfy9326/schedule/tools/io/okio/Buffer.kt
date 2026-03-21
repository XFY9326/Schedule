package tool.xfy9326.schedule.tools.io.okio

import okio.BufferedSink
import okio.BufferedSource
import okio.Sink
import okio.Source
import okio.buffer

inline fun <T> Source.useBuffer(block: BufferedSource.() -> T) = buffer().use(block)

inline fun <T> Sink.useBuffer(flush: Boolean = true, block: BufferedSink.() -> T) = buffer().use {
    try {
        it.block()
    } finally {
        if (flush && it.isOpen) it.flush()
    }
}