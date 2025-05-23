import enums.IniEntryType

class IniSection(
    val name: String,
    val entries: MutableList<IniEntry> = mutableListOf(),
) {
    override fun toString(): String {
        return "[$name]\n" + entries.joinToString("\n") { it.toString() }
    }

    fun addKey(key: String, value: Boolean?, capitalized: Boolean = true) {
        val entry = IniEntry(key, IniValue(value, capitalized), IniEntryType.Plain)
        entries.add(entry)
    }

    fun addKey(key: String, value: Int?) {
        val entry = IniEntry(key, IniValue(value), IniEntryType.Plain)
        entries.add(entry)
    }

    fun addKey(key: String, value: Float?) {
        val entry = IniEntry(key, IniValue(value), IniEntryType.Plain)
        entries.add(entry)
    }

    fun addKey(key: String, value: String?) {
        val entry = IniEntry(key, IniValue(value), IniEntryType.Plain)
        entries.add(entry)
    }

    fun addKey(key: String, value: Struct?) {
        val entry = IniEntry(key, IniValue(value), IniEntryType.Plain)
        entries.add(entry)
    }

    fun addArrayKey(key: String, value: List<Any?>, type: IniEntryType) {
        when (type) {
            IniEntryType.CommaSeparatedArray, IniEntryType.IndexedArray, IniEntryType.RepeatedLineArray -> {
                val list = value.map { item ->
                    when (item) {
                        is IniValue -> item
                        is String -> IniValue(item)
                        is Boolean -> IniValue(item)
                        is Int -> IniValue(item)
                        is Float -> IniValue(item)
                        is Map<*, *> -> IniValue(item as Map<String, Any?>)
                        null -> IniValue(null as String?)
                        else -> throw IllegalArgumentException("Invalid type for array item: ${item::class.java}")
                    }
                }
                val entry = IniEntry(key, list, type)
                entries.add(entry)
            }
            else -> throw IllegalArgumentException("Invalid type for array: $type")
        }
    }

    fun addMapKey(key: String, value: Map<String, Any?>) {
        val map = value.mapValues { entry ->
            when (val item = entry.value) {
                is IniValue -> item
                is String -> IniValue(item)
                is Boolean -> IniValue(item)
                is Int -> IniValue(item)
                is Float -> IniValue(item)
                is Map<*, *> -> IniValue(item as Map<String, Any?>)
                null -> IniValue(null as String?)
                else -> throw IllegalArgumentException("Invalid type for map value: ${item::class.java}")
            }
        }
        val entry = IniEntry(key, map, IniEntryType.Map)
        entries.add(entry)
    }

    suspend fun getBooleanKey(key: String): Boolean? {
        val entry = entries.find { it.key == key }
        return entry?.getBoolean()
    }

    suspend fun getIntegerKey(key: String): Int? {
        val entry = entries.find { it.key == key }
        return entry?.getInteger()
    }

    suspend fun getFloatKey(key: String): Float? {
        val entry = entries.find { it.key == key }
        return entry?.getFloat()
    }

    suspend fun getStringKey(key: String): String? {
        val entry = entries.find { it.key == key }
        return entry?.getString()
    }

    suspend fun getStructKey(key: String): Map<String, Any?>? {
        val entry = entries.find { it.key == key }
        return entry?.getStruct()
    }

    suspend fun getArrayKey(key: String): List<Any?>? {
        val entry = entries.find { it.key == key }
        return entry?.getArrayValues()
    }

    suspend fun getMapKey(key: String): Map<String, Any?>? {
        val entry = entries.find { it.key == key }
        return entry?.getMapValues()
    }

    fun getKeyType(key: String): IniEntryType? {
        val entry = entries.find { it.key == key }
        return entry?.type
    }

    suspend fun getKey(key: String): Any? {
        val entry = entries.find { it.key == key }
        return entry?.getValue()?.getValue()
    }
}