sealed class Value
data class StringValue(val value: String) : Value()
data class IntValue(val value: Int) : Value()
data class FloatValue(val value: Float) : Value()
data class BoolValue(val value: Boolean, val capitalized: Boolean = true) : Value()
data class StructValue(val fields: Map<String, Any?>) : Value()

sealed class Entry {
    abstract val key: String
}
data class Plain(override val key: String, val value: Value) : Entry() { // Key=Value
    override fun toString(): String {
        return "Plain(key='$key', value=$value)"
    }
}
data class CommaSeparatedArray(override val key: String, val values: List<Value>) : Entry() { // Key=Value1,Value2
    override fun toString(): String {
        return "CommaSeparatedArray(key='$key', values=$values)"
    }
}
data class RepeatedLineArray(override val key: String, val values: List<Value>) : Entry() { // Key=Value1\nKey=Value2
    override fun toString(): String {
        return "RepeatedLineArray(key='$key', values=$values)"
    }
}
data class IndexedArray(override val key: String, val indexedValues: Map<Int, Value>) : Entry() { // Key[0]=Value1\nKey[1]=Value2
    val values: List<Value>
        get() = indexedValues.values.toList()

    fun getIndices(): List<Int> = indexedValues.keys.toList()

    override fun toString(): String {
        return "IndexedArray(key='$key', indexedValues=$indexedValues)"
    }
}
data class MapEntry(override val key: String, val value: Map<String, Value>) : Entry() { // Key[key]=Value\nKey[key2]=Value2
    override fun toString(): String {
        return "MapEntry(key='$key', value=$value)"
    }
}

data class Section(val name: String, val entries: List<Entry>)
data class IniFile(val sections: List<Section>)
