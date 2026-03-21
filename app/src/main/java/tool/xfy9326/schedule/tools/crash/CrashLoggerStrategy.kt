package tool.xfy9326.schedule.tools.crash

import java.io.File

data class CrashLoggerStrategy(
    val crashLogDir: File,
    val maxLogAmount: Int = 10,
    val crashLogGenerator: CrashLogGenerator = DefaultCrashLogGenerator(),
    val useDefaultExceptionHandler: Boolean = true
)
