@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.ksp.base

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import kotlin.reflect.KClass

abstract class AbstractClassAnnotationSymbolProcessor<T : Annotation>(
    environment: SymbolProcessorEnvironment,
    private val annotationKClass: KClass<T>,
) : SymbolProcessor {
    protected val codeGenerator: CodeGenerator = environment.codeGenerator
    protected val logger: KSPLogger = environment.logger

    final override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotationName = annotationKClass.qualifiedName ?: return emptyList()
        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
        val result = ArrayList<KSAnnotated>()
        val classes = ArrayList<AnnotatedClass<T>>()
        try {
            symbols.forEach {
                if (it is KSClassDeclaration) {
                    if (it.validate()) {
                        val annotations = it.getAnnotationsByType(annotationKClass).toList()
                        if (annotations.isEmpty()) {
                            result.add(it)
                        } else {
                            classes.add(AnnotatedClass(it, annotations))
                        }
                    } else {
                        result.add(it)
                    }
                }
            }
            if (classes.isNotEmpty()) {
                process(classes)
            }
        } catch (e: Exception) {
            logger.exception(e)
        }
        return result
    }

    abstract fun process(classes: List<AnnotatedClass<T>>)
}