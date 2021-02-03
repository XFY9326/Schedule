@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

import java.io.Serializable

/**
 * Base course provider
 * 注：由于通过反射初始化该类，因此继承自该类的构造器仅允许以下两种形式
 * 1. ***Provider : ***Provider<Nothing>(null)
 * 2. ***Provider<P : Serializable>(params: P?) : ***Provider<Nothing>(params)
 * 除此之外不能够有其他的构造器
 *
 * @param P 参数类型
 * @property params 具体参数
 * @constructor Create empty Base course provider
 */
abstract class BaseCourseProvider<P : Serializable>(protected val params: P?)