@file:Suppress("SameParameterValue")

package tool.xfy9326.schedule.annotation.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.io.Serializable
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.reflect.KClass

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(CourseImportConfigProcessor.CODE_ANNOTATION_CLASS)
@SupportedOptions(CourseImportConfigProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class CourseImportConfigProcessor : AbstractProcessor() {
    companion object {
        const val CODE_ANNOTATION_CLASS = "tool.xfy9326.schedule.annotation.CourseImportConfig"
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        private const val TARGET_PACKAGE_NAME = "tool.xfy9326.schedule.content"
        private const val TARGET_CLASS_NAME = "CourseImportConfigRegistry"
        private const val TARGET_PROPERTY_CONFIG_CLASSES = "configClasses"
        private const val TARGET_FUNCTION_GET_CONFIGS = "getConfigs"

        private val WILDCARD_SERIALIZABLE = WildcardTypeName.producerOf(Serializable::class)
        private val KOTLIN_LIST = List::class.asClassName()
        private val KOTLIN_CLASS = KClass::class.asClassName()
        private val KOTLIN_COROUTINES_WITH_CONTEXT = MemberName("kotlinx.coroutines", "withContext")
        private val KOTLIN_COROUTINES_DISPATCHERS_IO = MemberName("kotlinx.coroutines.Dispatchers", "IO")

        private const val BASE_COURSE_ADAPTER_CLASS_PACKAGE = "tool.xfy9326.schedule.content.base"
        private val ABSTRACT_IMPORT_PROVIDER =
            ClassName(BASE_COURSE_ADAPTER_CLASS_PACKAGE, "AbstractCourseProvider").parameterizedBy(WILDCARD_SERIALIZABLE)
        private val ABSTRACT_IMPORT_PARSER =
            ClassName(BASE_COURSE_ADAPTER_CLASS_PACKAGE, "AbstractCourseParser").parameterizedBy(WILDCARD_SERIALIZABLE)
        private val ABSTRACT_IMPORT_CONFIG =
            ClassName(BASE_COURSE_ADAPTER_CLASS_PACKAGE, "AbstractCourseImportConfig").parameterizedBy(
                WILDCARD_SERIALIZABLE,
                WildcardTypeName.producerOf(ABSTRACT_IMPORT_PROVIDER),
                WILDCARD_SERIALIZABLE,
                WildcardTypeName.producerOf(ABSTRACT_IMPORT_PARSER)
            )

        private val TARGET_PROPERTY_CONFIG_CLASSES_CLASS =
            KOTLIN_LIST.parameterizedBy(KOTLIN_CLASS.parameterizedBy(WildcardTypeName.producerOf(ABSTRACT_IMPORT_CONFIG)))

        private fun getParametersString(size: Int, codeStr: String) =
            Array(size) { codeStr }.joinToString()
    }

    private val kaptSourceDir
        get() = File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]!!)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            val configClasses = getAllAnnotationClassName(annotations, roundEnv).toTypedArray()
            if (configClasses.isEmpty()) return false

            val generateFile = FileSpec.builder(TARGET_PACKAGE_NAME, TARGET_CLASS_NAME)
            val generateClass = TypeSpec.objectBuilder(TARGET_CLASS_NAME)

            val configProperty = PropertySpec.builder(TARGET_PROPERTY_CONFIG_CLASSES, TARGET_PROPERTY_CONFIG_CLASSES_CLASS)
                .initializer("listOf(${getParametersString(configClasses.size, "%T::class")})", *configClasses)
                .build()

            val sortedConfigFunction = FunSpec.builder(TARGET_FUNCTION_GET_CONFIGS)
                .addModifiers(KModifier.SUSPEND)
                .addStatement("""
                    return %M(%M) {
                        ${TARGET_PROPERTY_CONFIG_CLASSES}.map {
                            it.java.newInstance()
                        }
                    }
                """.trimIndent(), KOTLIN_COROUTINES_WITH_CONTEXT, KOTLIN_COROUTINES_DISPATCHERS_IO)
                .build()

            generateClass.addProperty(configProperty)
            generateClass.addFunction(sortedConfigFunction)
            generateFile
                .addType(generateClass.build())
                .build()
                .writeTo(kaptSourceDir)
        } catch (e: Exception) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.stackTraceToString())
        }

        return true
    }

    private fun getAllAnnotationClassName(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): List<ClassName> {
        val result = ArrayList<ClassName>()

        for (annotation in annotations) {
            val elements = roundEnv.getElementsAnnotatedWith(annotation)
            for (element in elements) {
                val pkgName = processingEnv.elementUtils.getPackageOf(element).asType().toString()
                val classSimpleName = element.simpleName.toString()
                val className = ClassName(pkgName, classSimpleName)
                result.add(className)
            }
        }

        return result
    }
}