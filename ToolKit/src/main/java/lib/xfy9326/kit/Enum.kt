@file:Suppress("unused")

package lib.xfy9326.kit

inline fun <reified E : Enum<E>> tryEnumValueOf(name: String?): E? {
    return if (name == null) {
        null
    } else {
        try {
            enumValueOf<E>(name)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

inline fun <reified E : Enum<E>> tryEnumValueOf(names: Set<String>?): Set<E>? {
    return if (names == null) {
        null
    } else {
        HashSet<E>(names.size).apply {
            names.forEach {
                tryEnumValueOf<E>(it)?.let(::add)
            }
        }
    }
}