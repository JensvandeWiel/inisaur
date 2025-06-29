import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Base class for all value types that can be stored in INI files.
 *
 * The [Value] hierarchy represents the different data types that can be
 * represented in INI files, including strings, integers, floats, booleans,
 * and structured data.
 */
@Serializable
sealed class Value

/**
 * Represents a string value in an INI file.
 *
 * String values are stored as-is unless they contain special characters,
 * in which case they are wrapped in quotes.
 *
 * @property value The string value, which may be null
 */
@Serializable
data class StringValue(val value: String?) : Value() {
    override fun toString(): String {
        return when {
            value == null -> ""
            else -> escapeStringForIni(value)
        }
    }

    /**
     * Escapes special characters in strings for INI format.
     *
     * This method ensures that strings containing newlines and other special characters
     * are properly escaped to maintain valid INI format.
     *
     * @param value The string value to escape
     * @return The escaped string suitable for INI representation
     */
    private fun escapeStringForIni(value: String): String {
        val newVal = value
            .replace("\\", "\\\\")  // Escape backslashes first
            .replace("\n", "\\n")   // Escape newlines
            .replace("\r", "\\r")   // Escape carriage returns
            .replace("\t", "\\t")   // Escape tabs

        return when {
            newVal.contains(Regex("[_,;=#\\[\\]\\n\\r\\t@#$%^&*()]|\\\\u[0-9a-fA-F]{4}|\\\\.")) -> "\"$newVal\""
            else -> newVal
        }
    }
}

/**
 * Represents an integer value in an INI file.
 *
 * @property value The integer value, which may be null
 */
@Serializable
data class IntValue(val value: Int?) : Value() {
    override fun toString(): String {
        return value?.toString() ?: ""
    }
}

/**
 * Represents a floating-point value in an INI file.
 *
 * @property value The float value, which may be null
 */
@Serializable
data class FloatValue(val value: Float?) : Value() {
    override fun toString(): String {
        return value?.toString() ?: ""
    }
}

/**
 * Represents a boolean value in an INI file.
 *
 * Boolean values can be represented as either capitalized (True/False) or
 * lowercase (true/false) depending on the [capitalized] property.
 *
 * @property value The boolean value, which may be null
 * @property capitalized Whether to use capitalized (True/False) or lowercase (true/false) format
 */
@Serializable
data class BoolValue(val value: Boolean?, val capitalized: Boolean = true) : Value() {
    override fun toString(): String {
        return when (value) {
            null -> ""
            true -> if (capitalized) "True" else "true"
            false -> if (capitalized) "False" else "false"
        }
    }
}

/**
 * Represents a structured value in an INI file.
 *
 * Struct values are represented as nested key-value pairs within parentheses,
 * such as `(key1=value1, key2=value2)`.
 *
 * @property fields The map of field names to field values
 */
@Serializable
data class StructValue(val fields: Map<String, Value?>) : Value() {
    override fun toString(): String {
        return fields.entries.joinToString(", ", "(", ")") { (key, value) ->
            "$key=${formatStructValue(value)}"
        }
    }

    /**
     * Converts this struct value to a map of native Kotlin types.
     *
     * @return A map where keys are field names and values are native Kotlin types
     * @throws IllegalArgumentException if any value has an unsupported type
     */
    fun toMap(): Map<String, Any?> {
        return fields.mapValues { (_, value) ->
            when (value) {
                is StringValue -> value.value
                is IntValue -> value.value
                is FloatValue -> value.value
                is BoolValue -> value.value
                is StructValue -> value.toMap()
                null -> null
                else -> throw IllegalArgumentException("Unsupported value type: ${value::class.java}")
            }
        }
    }

    /**
     * Formats a value for string representation within a struct.
     *
     * @param value The value to format
     * @return String representation of the value
     */
    private fun formatStructValue(value: Any?): String {
        return when (value) {
            null -> ""
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                (value as Map<String, Any?>).entries.joinToString(", ", "(", ")") { (k, v) ->
                    "$k=${formatStructValue(v)}"
                }
            }

            is Value -> value.toString()
            else -> value.toString()
        }
    }
}
