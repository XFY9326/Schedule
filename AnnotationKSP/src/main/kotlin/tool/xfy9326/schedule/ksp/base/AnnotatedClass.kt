package tool.xfy9326.schedule.ksp.base

import com.google.devtools.ksp.symbol.KSClassDeclaration

data class AnnotatedClass<T : Annotation>(
    val ksClassDeclaration: KSClassDeclaration,
    val annotations: List<T>,
)