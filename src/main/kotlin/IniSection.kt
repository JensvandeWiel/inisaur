import enums.IniEntryType
import java.security.Key

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

    fun addArrayKey(key: String, value: List<IniValue?>, type: IniEntryType) {
        when (type) {
            IniEntryType.CommaSeparatedArray, IniEntryType.IndexedArray, IniEntryType.RepeatedLineArray -> {
                val entry = IniEntry(key, value, type)
                entries.add(entry)
            }
            else -> throw IllegalArgumentException("Invalid type for array: $type")
        }
    }

    fun addMapKey(key: String, value: Map<String, IniValue?>) {
        val entry = IniEntry(key, value, IniEntryType.Map)
        entries.add(entry)
    }


}