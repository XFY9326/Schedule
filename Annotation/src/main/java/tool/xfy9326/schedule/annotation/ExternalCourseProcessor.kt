package tool.xfy9326.schedule.annotation

/**
 * External course processor
 * 用于标记该类将被注册为外部课程处理器
 * 仅在AbstractExternalCourseProcessor子类使用有效
 * @property name 注册的名称
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class ExternalCourseProcessor(
    val name: String,
)
