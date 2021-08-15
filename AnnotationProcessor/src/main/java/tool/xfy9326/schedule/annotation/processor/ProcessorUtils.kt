package tool.xfy9326.schedule.annotation.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import java.io.Serializable
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

object ProcessorUtils {
    const val PACKAGE_NAME = "tool.xfy9326.schedule"

    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

    val WILDCARD_SERIALIZABLE = WildcardTypeName.producerOf(Serializable::class)
    val KOTLIN_LIST = List::class.asClassName()
    val KOTLIN_CLASS = KClass::class.asClassName()
    val KOTLIN_COROUTINES_WITH_CONTEXT = MemberName("kotlinx.coroutines", "withContext")
    val KOTLIN_COROUTINES_DISPATCHERS_IO = MemberName("kotlinx.coroutines.Dispatchers", "IO")

    fun ProcessingEnvironment.getAnnotatedClassNames(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): List<ClassName> {
        val result = ArrayList<ClassName>()

        for (annotation in annotations) {
            val elements = roundEnv.getElementsAnnotatedWith(annotation)
            for (element in elements) {
                val pkgName = elementUtils.getPackageOf(element).asType().toString()
                val classSimpleName = element.simpleName.toString()
                val className = ClassName(pkgName, classSimpleName)
                result.add(className)
            }
        }

        return result
    }
}