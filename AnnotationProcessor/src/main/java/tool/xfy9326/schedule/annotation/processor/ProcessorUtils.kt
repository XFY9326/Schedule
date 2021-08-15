package tool.xfy9326.schedule.annotation.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

object ProcessorUtils {
    const val PACKAGE_NAME = "tool.xfy9326.schedule"

    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

    const val INDENT = "    "

    val KOTLIN_LIST = List::class.asClassName()
    val KOTLIN_MAP = Map::class.asClassName()
    val KOTLIN_CLASS = KClass::class.asClassName()
    val KOTLIN_COROUTINES_WITH_CONTEXT = MemberName("kotlinx.coroutines", "withContext")
    val KOTLIN_COROUTINES_DISPATCHERS_IO = MemberName("kotlinx.coroutines.Dispatchers", "IO")

    val ProcessingEnvironment.kaptSourceDir
        get() = File(options[KAPT_KOTLIN_GENERATED_OPTION_NAME]!!)

    fun <A : Annotation> ProcessingEnvironment.getAnnotatedClassNames(
        clazz: KClass<A>,
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment,
    ): ArrayList<Pair<A, ClassName>> {
        val result = ArrayList<Pair<A, ClassName>>()

        for (annotation in annotations) {
            val elements = roundEnv.getElementsAnnotatedWith(annotation)
            for (element in elements) {
                val pkgName = elementUtils.getPackageOf(element).asType().toString()
                val classSimpleName = element.simpleName.toString()
                val className = ClassName(pkgName, classSimpleName)
                val annotationObject = element.getAnnotation(clazz.java)
                result.add(annotationObject to className)
            }
        }

        return result
    }

    fun getParametersString(size: Int, codeStr: String) = Array(size) { codeStr }.joinToString()
}