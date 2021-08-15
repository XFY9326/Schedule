@file:Suppress("SameParameterValue")

package tool.xfy9326.schedule.annotation.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import tool.xfy9326.schedule.annotation.processor.ProcessorUtils.getAnnotatedClassNames
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(CourseImportConfigProcessor.CODE_ANNOTATION_CLASS)
@SupportedOptions(ProcessorUtils.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class CourseImportConfigProcessor : AbstractProcessor() {
    companion object {
        const val CODE_ANNOTATION_CLASS = "${ProcessorUtils.PACKAGE_NAME}.annotation.CourseImportConfig"

        private const val TARGET_PACKAGE_NAME = "${ProcessorUtils.PACKAGE_NAME}.content"
        private const val TARGET_CLASS_NAME = "CourseImportConfigRegistry"
        private const val TARGET_PROPERTY_CONFIG_CLASSES = "configClasses"
        private const val TARGET_FUNCTION_GET_CONFIGS = "getConfigs"

        private const val BASE_COURSE_ADAPTER_CLASS_PACKAGE = "${ProcessorUtils.PACKAGE_NAME}.content.base"
        private val ABSTRACT_IMPORT_PROVIDER =
            ClassName(BASE_COURSE_ADAPTER_CLASS_PACKAGE, "AbstractCourseProvider").parameterizedBy(ProcessorUtils.WILDCARD_SERIALIZABLE)
        private val ABSTRACT_IMPORT_PARSER =
            ClassName(BASE_COURSE_ADAPTER_CLASS_PACKAGE, "AbstractCourseParser").parameterizedBy(ProcessorUtils.WILDCARD_SERIALIZABLE)
        private val ABSTRACT_IMPORT_CONFIG =
            ClassName(BASE_COURSE_ADAPTER_CLASS_PACKAGE, "AbstractCourseImportConfig").parameterizedBy(
                ProcessorUtils.WILDCARD_SERIALIZABLE,
                WildcardTypeName.producerOf(ABSTRACT_IMPORT_PROVIDER),
                ProcessorUtils.WILDCARD_SERIALIZABLE,
                WildcardTypeName.producerOf(ABSTRACT_IMPORT_PARSER)
            )

        private val TARGET_PROPERTY_CONFIG_CLASSES_CLASS =
            ProcessorUtils.KOTLIN_LIST.parameterizedBy(ProcessorUtils.KOTLIN_CLASS.parameterizedBy(WildcardTypeName.producerOf(ABSTRACT_IMPORT_CONFIG)))

        private fun getParametersString(size: Int, codeStr: String) =
            Array(size) { codeStr }.joinToString()
    }

    private val kaptSourceDir
        get() = File(processingEnv.options[ProcessorUtils.KAPT_KOTLIN_GENERATED_OPTION_NAME]!!)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            val configClasses = processingEnv.getAnnotatedClassNames(annotations, roundEnv).toTypedArray()
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
                """.trimIndent(), ProcessorUtils.KOTLIN_COROUTINES_WITH_CONTEXT, ProcessorUtils.KOTLIN_COROUTINES_DISPATCHERS_IO)
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
}