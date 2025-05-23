import enums.IniValueType

typealias Struct = Map<String, Any?>

/**
 * Converts a [Struct] to an [IniValue].
 * Should be used when the [Struct] is being added in a nested form.
 *
 * @return The [IniValue] representation of the [Struct].
 */
fun Struct.toIniValue(): IniValue {
    return IniValue(this, IniValueType.Struct)
}

fun structOf(vararg pairs: Pair<String, Any?>): Struct {
    val map = mutableMapOf<String, Any?>()
    for (pair in pairs) {
        val key = pair.first
        val value = pair.second
        map[key] = when (value) {
            is Map<*, *> -> (value as Struct).toIniValue()
            is String -> IniValue(value)
            is Boolean -> IniValue(value)
            is Int -> IniValue(value)
            is Float -> IniValue(value)
            null -> IniValue(null as String?)
            is IniValue -> value
            else -> throw IllegalArgumentException("Invalid type for struct value: ${value::class.java}")
        }
    }

    return map
}

fun structOfAsIniValue(vararg pairs: Pair<String, IniValue>): IniValue {
    return structOf(*pairs).toIniValue()
}

fun Struct.underlyingValue(): Map<String, Any?> {
    return this.mapValues { if (it.value is IniValue) (it.value as IniValue).getValue() else it.value }
}