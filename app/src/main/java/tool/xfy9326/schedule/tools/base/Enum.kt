package tool.xfy9326.schedule.tools.base

inline fun <reified E : Enum<E>> tryEnumValueOf(name: String): E? {
    return runCatching {
        enumValueOf<E>(name)
    }.getOrNull()
}

inline fun <reified E : Enum<E>> tryEnumSetValueOf(names: Set<String>): Set<E> {
    return names.mapNotNull {
        tryEnumValueOf<E>(it)
    }.toSet()
}
