package tool.xfy9326.schedule.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import tool.xfy9326.schedule.annotation.ExternalCourseProcessor
import tool.xfy9326.schedule.ksp.base.AbstractClassAnnotationSymbolProcessor
import tool.xfy9326.schedule.ksp.base.AnnotatedClass

class ExternalCourseProcessorProcessor(environment: SymbolProcessorEnvironment) :
    AbstractClassAnnotationSymbolProcessor<ExternalCourseProcessor>(environment, ExternalCourseProcessor::class) {
    companion object {
        private const val TARGET_PACKAGE_NAME = "$PackageName.content"
        private const val TARGET_CLASS_NAME = "ExternalCourseProcessorRegistry"
        private const val TARGET_PROPERTY_PROCESSOR_CLASSES = "processorClasses"
        private const val TARGET_FUNCTION_GET_PROCESSOR = "getProcessor"
        private const val TARGET_FUNCTION_GET_PROCESSOR_PARAM = "name"

        private val ABSTRACT_EXTERNAL_PROCESSOR =
            ClassName("$PackageName.content.base", "AbstractExternalCourseProcessor").parameterizedBy(STAR, STAR, STAR)

        private val TARGET_PROPERTY_PROCESSOR_CLASSES_CLASS =
            KOTLIN_MAP.parameterizedBy(STRING, KOTLIN_CLASS.parameterizedBy(WildcardTypeName.producerOf(ABSTRACT_EXTERNAL_PROCESSOR)))
    }

    override fun process(classes: List<AnnotatedClass<ExternalCourseProcessor>>) {
        createProcessorRegistry(
            classes.map {
                it.annotations.first().name to it.ksClassDeclaration.toClassName()
            }
        ).writeTo(codeGenerator, true)
    }

    private fun createProcessorRegistry(processorClasses: List<Pair<String, ClassName>>): FileSpec {
        val generateFile = FileSpec.builder(TARGET_PACKAGE_NAME, TARGET_CLASS_NAME)
        val generateClass = TypeSpec.objectBuilder(TARGET_CLASS_NAME)

        val outputParamsList = Array(processorClasses.size * 2) {
            if (it % 2 == 0) {
                processorClasses[it].first
            } else {
                processorClasses[it / 2].second
            }
        }

        val mapProperty = PropertySpec.builder(TARGET_PROPERTY_PROCESSOR_CLASSES, TARGET_PROPERTY_PROCESSOR_CLASSES_CLASS)
            .initializer("mapOf(${createParametersString(outputParamsList.size / 2, "%S to %T::class")})", *outputParamsList)
            .addModifiers(KModifier.PRIVATE)
            .build()

        val getProcessorFunction = FunSpec.builder(TARGET_FUNCTION_GET_PROCESSOR)
            .addParameter(TARGET_FUNCTION_GET_PROCESSOR_PARAM, STRING)
            .addStatement(
                """
                    return $TARGET_PROPERTY_PROCESSOR_CLASSES[$TARGET_FUNCTION_GET_PROCESSOR_PARAM]?.java?.getConstructor()?.newInstance()
                """.trimIndent()
            )
            .returns(ABSTRACT_EXTERNAL_PROCESSOR.copy(nullable = true))
            .build()

        generateClass.addProperty(mapProperty)
        generateClass.addFunction(getProcessorFunction)
        return generateFile
            .addType(generateClass.build())
            .build()
    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return ExternalCourseProcessorProcessor(environment)
        }
    }
}