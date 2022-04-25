package tool.xfy9326.schedule.annotation

import androidx.annotation.Dimension

@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE
)
@Dimension(unit = Dimension.DP)
annotation class Dp