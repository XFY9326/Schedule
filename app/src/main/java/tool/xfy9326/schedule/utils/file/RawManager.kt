package tool.xfy9326.schedule.utils.file

import lib.xfy9326.io.processor.textReader
import lib.xfy9326.io.target.createRawTarget
import tool.xfy9326.schedule.R

object RawManager {
    private val EULA_TXT_READER by lazy { createRawTarget(R.raw.eula).textReader() }
    private val LICENSE_TXT_READER by lazy { createRawTarget(R.raw.license).textReader() }

    suspend fun readEULA() = EULA_TXT_READER.read()!!

    suspend fun readOpenSourceLicense() = LICENSE_TXT_READER.read()!!
}