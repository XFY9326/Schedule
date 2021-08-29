import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import java.util.concurrent.TimeUnit

val Project.GitCommitShortId
    get() = executeCommand("git rev-parse --short HEAD")

inline fun <T> NamedDomainObjectCollection<T>.withDebug(crossinline action: T.() -> Unit) {
    getByName("debug") { action(this) }
}

inline fun <T> NamedDomainObjectCollection<T>.withRelease(crossinline action: T.() -> Unit) {
    getByName("release") { action(this) }
}

fun Project.executeCommand(command: String, waitSeconds: Long = -1L): String {
    val commandArray = command.split("\\s".toRegex()).toTypedArray()
    val processorBuilder = ProcessBuilder(*commandArray)
        .directory(project.rootDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)

    var process: Process? = null
    try {
        process = processorBuilder.start()
        if (waitSeconds < 0) {
            process.waitFor()
        } else {
            process.waitFor(waitSeconds, TimeUnit.SECONDS)
        }
        return process.inputStream.bufferedReader().readText().trim()
    } finally {
        process?.destroy()
    }
}