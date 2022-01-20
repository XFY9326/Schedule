package tool.xfy9326.schedule.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo
import tool.xfy9326.schedule.annotation.CourseImportConfig
import tool.xfy9326.schedule.ksp.base.AbstractClassAnnotationSymbolProcessor
import tool.xfy9326.schedule.ksp.base.AnnotatedClass

class CourseImportConfigProcessor(environment: SymbolProcessorEnvironment) :
    AbstractClassAnnotationSymbolProcessor<CourseImportConfig>(environment, CourseImportConfig::class) {
    companion object {
        private const val TARGET_PACKAGE_NAME = "$PackageName.content"
        private const val TARGET_CLASS_NAME = "CourseImportConfigRegistry"
        private const val TARGET_PROPERTY_CONFIG_CLASSES = "configClasses"
        private const val TARGET_FUNCTION_GET_CONFIGS = "getConfigs"

        private val ABSTRACT_IMPORT_CONFIG =
            ClassName("${PackageName}.content.base", "AbstractCourseImportConfig").parameterizedBy(STAR, STAR, STAR, STAR)

        private val TARGET_PROPERTY_CONFIG_CLASSES_CLASS = KOTLIN_LIST.parameterizedBy(KOTLIN_CLASS.parameterizedBy(WildcardTypeName.producerOf(ABSTRACT_IMPORT_CONFIG)))
    }

    override fun process(classes: List<AnnotatedClass<CourseImportConfig>>) {
        createConfigRegistry(classes.toClassNames()).writeTo(codeGenerator, true)
    }

    private fun createConfigRegistry(configClasses: Array<ClassName>): FileSpec {
        val generateFile = FileSpec.builder(TARGET_PACKAGE_NAME, TARGET_CLASS_NAME)
        val generateClass = TypeSpec.objectBuilder(TARGET_CLASS_NAME)

        val configProperty = PropertySpec.builder(TARGET_PROPERTY_CONFIG_CLASSES, TARGET_PROPERTY_CONFIG_CLASSES_CLASS)
            .initializer("listOf(${createParametersString(configClasses.size, "%T::class")})", *configClasses)
            .addModifiers(KModifier.PRIVATE)
            .build()

        val configFunction = FunSpec.builder(TARGET_FUNCTION_GET_CONFIGS)
            .addModifiers(KModifier.SUSPEND)
            .addStatement("""
                    return %M(%M) {
                        $TARGET_PROPERTY_CONFIG_CLASSES.map {
                            it.java.newInstance()
                        }
                    }
                """.trimIndent(), KOTLIN_COROUTINES_WITH_CONTEXT, KOTLIN_COROUTINES_DISPATCHERS_IO)
            .build()

        generateClass.addProperty(configProperty)
        generateClass.addFunction(configFunction)
        return generateFile
            .addType(generateClass.build())
            .build()
    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return CourseImportConfigProcessor(environment)
        }
    }
}