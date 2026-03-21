package tool.xfy9326.schedule.tools.base

inline fun <reified E : Enum<E>> tryEnumValueOf(name: String): E? {
    return try {
        enumValueOf<E>(name)
    } catch (e: Exception) {
        null
    }
}

inline fun <reified E : Enum<E>> tryEnumSetValueOf(names: Set<String>): Set<E> {
    return names.mapNotNull {
        tryEnumValueOf<E>(it)
    }.toSet()
}
