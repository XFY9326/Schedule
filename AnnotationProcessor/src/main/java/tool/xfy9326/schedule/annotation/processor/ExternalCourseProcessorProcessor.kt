package tool.xfy9326.schedule.annotation.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import tool.xfy9326.schedule.annotation.ExternalCourseProcessor
import tool.xfy9326.schedule.annotation.processor.ProcessorUtils.getAnnotatedClassNames
import tool.xfy9326.schedule.annotation.processor.ProcessorUtils.kaptSourceDir
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(ExternalCourseProcessorProcessor.CODE_ANNOTATION_CLASS)
@SupportedOptions(ProcessorUtils.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class ExternalCourseProcessorProcessor : AbstractProcessor() {
    companion object {
        const val CODE_ANNOTATION_CLASS = "${ProcessorUtils.PACKAGE_NAME}.annotation.ExternalCourseProcessor"

        private const val TARGET_PACKAGE_NAME = "${ProcessorUtils.PACKAGE_NAME}.content"
        private const val TARGET_CLASS_NAME = "ExternalCourseProcessorRegistry"
        private const val TARGET_PROPERTY_PROCESSOR_CLASSES = "processorClasses"
        private const val TARGET_FUNCTION_GET_PROCESSOR = "getProcessor"
        private const val TARGET_FUNCTION_GET_PROCESSOR_PARAM = "name"

        private val ABSTRACT_EXTERNAL_PROCESSOR =
            ClassName("${ProcessorUtils.PACKAGE_NAME}.content.base", "AbstractExternalCourseProcessor").parameterizedBy(STAR, STAR, STAR)

        private val TARGET_PROPERTY_PROCESSOR_CLASSES_CLASS =
            ProcessorUtils.KOTLIN_MAP.parameterizedBy(STRING, ProcessorUtils.KOTLIN_CLASS.parameterizedBy(WildcardTypeName.producerOf(ABSTRACT_EXTERNAL_PROCESSOR)))
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            val processorClasses = processingEnv.getAnnotatedClassNames(ExternalCourseProcessor::class, annotations, roundEnv)
            if (processorClasses.isEmpty()) return false

            val generateFile = FileSpec.builder(TARGET_PACKAGE_NAME, TARGET_CLASS_NAME)
            val generateClass = TypeSpec.objectBuilder(TARGET_CLASS_NAME)

            val outputParamsList = Array(processorClasses.size * 2) {
                if (it % 2 == 0) {
                    processorClasses[it].first.name
                } else {
                    processorClasses[it / 2].second
                }
            }

            val mapProperty = PropertySpec.builder(TARGET_PROPERTY_PROCESSOR_CLASSES, TARGET_PROPERTY_PROCESSOR_CLASSES_CLASS)
                .initializer("mapOf(${ProcessorUtils.getParametersString(outputParamsList.size / 2, "%S to %T::class")})", *outputParamsList)
                .addModifiers(KModifier.PRIVATE)
                .build()

            val getProcessorFunction = FunSpec.builder(TARGET_FUNCTION_GET_PROCESSOR)
                .addParameter(TARGET_FUNCTION_GET_PROCESSOR_PARAM, STRING)
                .addStatement("""
                    return $TARGET_PROPERTY_PROCESSOR_CLASSES[$TARGET_FUNCTION_GET_PROCESSOR_PARAM]?.java?.newInstance()
                """.trimIndent())
                .build()

            generateClass.addProperty(mapProperty)
            generateClass.addFunction(getProcessorFunction)
            generateFile
                .addType(generateClass.build())
                .indent(ProcessorUtils.INDENT)
                .build()
                .writeTo(processingEnv.kaptSourceDir)
        } catch (e: Exception) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.stackTraceToString())
        }

        return true
    }
}