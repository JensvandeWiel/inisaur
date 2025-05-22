import enums.IniValueType

typealias Struct = Map<String, IniValue>

/**
 * Converts a [Struct] to an [IniValue].
 * Should be used when the [Struct] is being added in a nested form.
 *
 * @return The [IniValue] representation of the [Struct].
 */
fun Struct.toIniValue(): IniValue {
    return IniValue(this, IniValueType.Struct)
}

fun newStruct(initial: Struct): Struct {
    return mutableMapOf()
}