/**
 * Base class for all types of entries in an INI file.
 *
 * An entry represents a key with an associated value (or values) in an INI file.
 * Different subclasses represent different formats for storing values.
 *
 * @property key The key name for this entry
 */
sealed class Entry {
    abstract val key: String
}

/**
 * Represents a simple key-value entry in an INI file.
 *
 * Format: `Key=Value`
 *
 * @property key The key name
 * @property value The associated value
 */
data class Plain(override val key: String, val value: Value) : Entry() {
    override fun toString(): String {
        return "$key=$value"
    }
}

/**
 * Represents an array stored as comma-separated values in an INI file.
 *
 * Format: `Key=Value1,Value2,Value3`
 *
 * @property key The key name
 * @property values The list of values
 */
data class CommaSeparatedArray(override val key: String, val values: List<Value>) : Entry() {
    override fun toString(): String {
        return toCommaSeparatedString()
    }

    /**
     * Converts this array to a string with comma-separated values.
     *
     * @return A string representation of the array with comma-separated values
     */
    fun toCommaSeparatedString(): String {
        // For empty arrays, just return the key with equals sign
        if (values.isEmpty()) {
            return "$key="
        }

        // Find the last non-empty value index
        var lastNonEmptyIndex = values.lastIndex
        while (lastNonEmptyIndex >= 0 && values[lastNonEmptyIndex].toString().isEmpty()) {
            lastNonEmptyIndex--
        }

        // If all values are empty, return just the key
        if (lastNonEmptyIndex < 0) {
            return "$key="
        }

        // Join only up to the last non-empty value
        // Important: we need to keep empty strings in the middle to preserve positions
        val valuesToJoin = values.subList(0, lastNonEmptyIndex + 1)
        return "$key=${valuesToJoin.joinToString(",") { it.toString() }}"
    }

    /**
     * Converts the array values to a list of native Kotlin types.
     *
     * @return List of values converted to their native Kotlin types
     * @throws IllegalArgumentException if any value has an unsupported type
     */
    fun toList(): List<Any?> {
        return values.map { value ->
            when (value) {
                is StringValue -> value.value
                is IntValue -> value.value
                is FloatValue -> value.value
                is BoolValue -> value.value
                is StructValue -> value.toMap()
                else -> throw IllegalArgumentException("Unsupported value type: ${value::class.java}")
            }
        }
    }
}

/**
 * Represents an array stored as repeated keys on multiple lines.
 *
 * Format:
 * ```
 * Key=Value1
 * Key=Value2
 * Key=Value3
 * ```
 *
 * @property key The key name
 * @property values The list of values
 */
data class RepeatedLineArray(override val key: String, val values: List<Value>) : Entry() {
    override fun toString(): String {
        return values.joinToString("\n") { "$key=$it" }
    }

    /**
     * Converts the array values to a list of native Kotlin types.
     *
     * @return List of values converted to their native Kotlin types
     * @throws IllegalArgumentException if any value has an unsupported type
     */
    fun toList(): List<Any?> {
        return values.map { value ->
            when (value) {
                is StringValue -> value.value
                is IntValue -> value.value
                is FloatValue -> value.value
                is BoolValue -> value.value
                is StructValue -> value.toMap()
                else -> throw IllegalArgumentException("Unsupported value type: ${value::class.java}")
            }
        }
    }
}

/**
 * Represents an array with indexed keys.
 *
 * Format:
 * ```
 * Key[0]=Value1
 * Key[1]=Value2
 * Key[2]=Value3
 * ```
 *
 * @property key The base key name (without index)
 * @property indexedValues Map of indices to values
 */
data class IndexedArray(override val key: String, val indexedValues: Map<Int, Value>) : Entry() {
    /**
     * Gets the list of values in this indexed array.
     * Note that this may not preserve the order if indices are not sequential.
     *
     * @return List of all values in this indexed array
     */
    val values: List<Value>
        get() = indexedValues.values.toList()

    /**
     * Gets the list of indices used in this indexed array.
     *
     * @return List of all indices in this indexed array
     */
    fun getIndices(): List<Int> = indexedValues.keys.toList()

    override fun toString(): String {
        return indexedValues.entries.joinToString("\n") { (index, value) -> "$key[$index]=$value" }
    }

    /**
     * Converts the indexed values to a map of native Kotlin types.
     *
     * @return Map of indices to values converted to their native Kotlin types
     * @throws IllegalArgumentException if any value has an unsupported type
     */
    fun toMap(): Map<Int, Any?> {
        return indexedValues.mapValues { (_, value) ->
            when (value) {
                is StringValue -> value.value
                is IntValue -> value.value
                is FloatValue -> value.value
                is BoolValue -> value.value
                is StructValue -> value.toMap()
                else -> throw IllegalArgumentException("Unsupported value type: ${value::class.java}")
            }
        }
    }
}

/**
 * Represents a map with string keys.
 *
 * Format:
 * ```
 * Key[Name1]=Value1
 * Key[Name2]=Value2
 * Key[Name3]=Value3
 * ```
 *
 * @property key The base key name (without index)
 * @property value Map of string keys to values
 */
data class MapEntry(override val key: String, val value: Map<String, Value>) : Entry() {
    override fun toString(): String {
        return value.entries.joinToString("\n") { (k, v) -> "$key[$k]=$v" }
    }

    /**
     * Converts the map values to a map of native Kotlin types.
     *
     * @return Map of string keys to values converted to their native Kotlin types
     * @throws IllegalArgumentException if any value has an unsupported type
     */
    fun toMap(): Map<String, Any?> {
        return value.mapValues { (_, v) ->
            when (v) {
                is StringValue -> v.value
                is IntValue -> v.value
                is FloatValue -> v.value
                is BoolValue -> v.value
                is StructValue -> v.toMap()
                else -> throw IllegalArgumentException("Unsupported value type: ${v::class.java}")
            }
        }
    }
}

