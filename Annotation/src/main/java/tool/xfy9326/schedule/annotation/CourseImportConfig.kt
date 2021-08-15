package tool.xfy9326.schedule.annotation

/**
 * Course import config
 * 用于标记该类需要被注册为课程导入配置文件
 * 仅在AbstractCourseImportConfig子类使用有效
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class CourseImportConfig
