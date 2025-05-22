import enums.IniEntryType
import exceptions.InvalidTypeException

class IniEntry {
    val key: String
    val value: Any?
    val type: IniEntryType

    @Throws(InvalidTypeException::class)
    constructor(
        key: String,
        value: Any?,
        type: IniEntryType,
    ) {
        when (value) {
            is IniValue -> { /* ok */ }
            is List<*> -> {
                value.forEach { v ->
                    if (v !is IniValue && v != null) {
                        throw InvalidTypeException("Invalid type for IniEntry: ${v?.javaClass}")
                    }
                }
            }
            is Map<*, *> -> {
                value.forEach { entry ->
                    if (entry.value !is IniValue) {
                        throw InvalidTypeException("Invalid type for IniEntry: ${entry.value?.javaClass}")
                    }
                }
            }
            else -> throw InvalidTypeException("Invalid type for IniEntry: ${value?.javaClass}")
        }
        if (key.isEmpty()) {
            throw InvalidTypeException("Key cannot be empty")
        }
        this.key = key
        this.value = value
        this.type = type
    }

    private fun keyString(value: String): String {
        return "$key=$value"
    }

    @Throws(InvalidTypeException::class)
    override fun toString(): String {

        return when (type) {
            IniEntryType.Plain -> toStringValue()
            IniEntryType.CommaSeparatedArray -> toCommaSeparatedArrayString()
            IniEntryType.RepeatedLineArray -> toRepeatedLineArrayString()
            IniEntryType.IndexedArray -> toIndexedArrayString()
            IniEntryType.Map -> toMapString()
        }
    }

    @Throws(InvalidTypeException::class)
    private fun toStringValue(): String {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }
        return keyString(when (value) {
            is IniValue -> value.toString()
            null -> ""
            else -> throw InvalidTypeException("Invalid type for Plain: ${value::class.java}")
        })
    }

    @Throws(InvalidTypeException::class)
    private fun toCommaSeparatedArrayString(): String {
        if (type != IniEntryType.CommaSeparatedArray) {
            throw InvalidTypeException("Invalid type for CommaSeparatedArray: $type")
        }
        return keyString(when (value) {
            is List<*> -> value.joinToString(",")
            null -> ""
            else -> throw InvalidTypeException("Invalid type for CommaSeparatedArray: ${value::class.java}")
        })
    }

    @Throws(InvalidTypeException::class)
    private fun toRepeatedLineArrayString(): String {
        if (type != IniEntryType.RepeatedLineArray) {
            throw InvalidTypeException("Invalid type for RepeatedLineArray: $type")
        }
        return when (value) {
            is List<*> -> value
                .filter { it != null && it.toString().isNotEmpty() }
                .joinToString("\n") { "$key=$it" }
            null -> ""
            else -> throw InvalidTypeException("Invalid type for RepeatedLineArray: ${value::class.java}")
        }
    }

    @Throws(InvalidTypeException::class)
    private fun toIndexedArrayString(): String {
        if (type != IniEntryType.IndexedArray) {
            throw InvalidTypeException("Invalid type for IndexedArray: $type")
        }
        val parentKey = key
        return (when (value) {
            is List<*> -> value.fold("") { acc, v -> if (v == null || (v as IniValue).toString() == "") acc else "\n$acc$parentKey[${value.indexOf(v)}]=$v\n" }
            null -> ""
            else -> throw InvalidTypeException("Invalid type for IndexedArray: ${value::class.java}")
        }).trim()
    }

    @Throws(InvalidTypeException::class)
    private fun toMapString(): String {
        if (type != IniEntryType.Map) {
            throw InvalidTypeException("Invalid type for Map: $type")
        }
        return when (value) {
            is Map<*, *> -> value.entries.joinToString("\n") { "$key[${it.key}]=${it.value}" }
            null -> ""
            else -> throw InvalidTypeException("Invalid type for Map: ${value::class.java}")
        }
    }
}