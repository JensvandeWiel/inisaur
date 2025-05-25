sealed class Value

data class StringValue(val value: String?) : Value() {
    override fun toString(): String {
        // if contains special characters, wrap in quotes
        return when {
            value == null -> ""
            value.contains(Regex("[_,;=#\\[\\]\\n\\r\\t@#$%^&*()]|\\\\u[0-9a-fA-F]{4}|\\\\.")) -> "\"$value\""
            else -> value
        }
    }
}
data class IntValue(val value: Int?) : Value() {
    override fun toString(): String {
        return value?.toString() ?: ""
    }
}
data class FloatValue(val value: Float?) : Value() {
    override fun toString(): String {
        return value?.toString() ?: ""
    }
}
data class BoolValue(val value: Boolean?, val capitalized: Boolean = true) : Value() {
    override fun toString(): String {
        return when (value) {
            null -> ""
            true -> if (capitalized) "True" else "true"
            false -> if (capitalized) "False" else "false"
        }
    }
}



data class StructValue(val fields: Map<String, Value?>) : Value() {
    override fun toString(): String {
        if (fields == null) return ""

        return fields.entries.joinToString(", ", "(", ")") { (key, value) ->
            "$key=${formatStructValue(value)}"
        }
    }

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