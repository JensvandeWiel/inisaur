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

fun structOf(vararg pairs: Pair<String, IniValue>): Struct {
    return mapOf(*pairs)
}

fun structOfAsIniValue(vararg pairs: Pair<String, IniValue>): IniValue {
    return structOf(*pairs).toIniValue()
}

fun Struct.underlyingValue(): Map<String, Any?> {
    return this.mapValues { if (it.value is IniValue) (it.value as IniValue).getValue() else it.value }
}