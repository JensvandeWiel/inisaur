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

sealed class Entry {
    abstract val key: String
}
data class Plain(override val key: String, val value: Value) : Entry() { // Key=Value
    override fun toString(): String {
        return "$key=$value"
    }
}
data class CommaSeparatedArray(override val key: String, val values: List<Value>) : Entry() { // Key=Value1,Value2
    override fun toString(): String {
        return "$key=${values.joinToString(",")}"
    }

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
data class RepeatedLineArray(override val key: String, val values: List<Value>) : Entry() { // Key=Value1\nKey=Value2
    override fun toString(): String {
        return values.joinToString("\n") { "$key=$it" }
    }

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
data class IndexedArray(override val key: String, val indexedValues: Map<Int, Value>) : Entry() { // Key[0]=Value1\nKey[1]=Value2
    val values: List<Value>
        get() = indexedValues.values.toList()

    fun getIndices(): List<Int> = indexedValues.keys.toList()

    override fun toString(): String {
        return indexedValues.entries.joinToString("\n") { (index, value) -> "$key[$index]=$value" }
    }

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
data class MapEntry(override val key: String, val value: Map<String, Value>) : Entry() { // Key[key]=Value\nKey[key2]=Value2
    override fun toString(): String {
        return value.entries.joinToString("\n") { (k, v) -> "$key[$k]=$v" }
    }

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