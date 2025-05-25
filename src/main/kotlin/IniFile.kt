import exceptions.InvalidTypeException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class IniFile {
    private val _sections: MutableList<Section>
    private val mutex = Mutex()
    private val rwLock = ReentrantReadWriteLock()

    constructor(sections: List<Section>) {
        _sections = sections.toMutableList()
    }
    constructor() : this(emptyList())

    override fun toString(): String {
        return rwLock.read { _sections.joinToString("\n\n") }
    }

    val sections: List<Section>
        get() = rwLock.read { _sections.toList() }

    /**
     * Gets a section by name (blocking).
     * @throws NoSuchElementException if the section is not found.
     */
    @Throws(NoSuchElementException::class)
    fun getSection(name: String): Section {
        return rwLock.read {
            _sections.firstOrNull { it.name == name }
                ?: throw NoSuchElementException("Section '$name' not found")
        }
    }

    /**
     * Gets a section by name (suspendable).
     * @throws NoSuchElementException if the section is not found.
     */
    @Throws(NoSuchElementException::class)
    suspend fun getSectionAsync(name: String): Section {
        return mutex.withLock {
            _sections.firstOrNull { it.name == name }
                ?: throw NoSuchElementException("Section '$name' not found")
        }
    }

    /**
     * Checks if a section with the given name exists (blocking).
     * @return true if the section exists, false otherwise.
     */
    fun hasSection(name: String): Boolean {
        return rwLock.read { _sections.any { it.name == name } }
    }

    /**
     * Checks if a section with the given name exists (suspendable).
     * @return true if the section exists, false otherwise.
     */
    suspend fun hasSectionAsync(name: String): Boolean {
        return mutex.withLock { _sections.any { it.name == name } }
    }

    /**
     * Adds a new section with the given name (blocking).
     * If the section already exists, it is not added again.
     * @return the existing or newly added section.
     */
    fun addSection(name: String): Section {
        return rwLock.write {
            _sections.firstOrNull { it.name == name } ?: run {
                val newSection = Section(name)
                _sections.add(newSection)
                newSection
            }
        }
    }

    /**
     * Adds a new section with the given name (suspendable).
     * If the section already exists, it is not added again.
     * @return the existing or newly added section.
     */
    suspend fun addSectionAsync(name: String): Section {
        return mutex.withLock {
            _sections.firstOrNull { it.name == name } ?: run {
                val newSection = Section(name)
                _sections.add(newSection)
                newSection
            }
        }
    }

    /**
     * Deletes a section with the given name (blocking).
     * @throws NoSuchElementException if the section is not found.
     */
    @Throws(NoSuchElementException::class)
    fun deleteSection(name: String) {
        rwLock.write {
            val section = _sections.firstOrNull { it.name == name }
                ?: throw NoSuchElementException("Section '$name' not found")
            _sections.remove(section)
        }
    }

    /**
     * Deletes a section with the given name (suspendable).
     * @throws NoSuchElementException if the section is not found.
     */
    @Throws(NoSuchElementException::class)
    suspend fun deleteSectionAsync(name: String) {
        mutex.withLock {
            val section = _sections.firstOrNull { it.name == name }
                ?: throw NoSuchElementException("Section '$name' not found")
            _sections.remove(section)
        }
    }

    /**
     * Gets a value from a section directly (blocking).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getValue(sectionName: String, key: String): Value {
        return getSection(sectionName).getKey(key)
    }

    /**
     * Gets a value from a section directly (suspendable).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getValueAsync(sectionName: String, key: String): Value {
        return getSectionAsync(sectionName).getKeyAsync(key)
    }

    /**
     * Gets a string value from a section directly (blocking).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a string value or not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getStringValue(sectionName: String, key: String): String? {
        return getSection(sectionName).getStringKey(key)
    }

    /**
     * Gets a string value from a section directly (suspendable).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a string value or not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getStringValueAsync(sectionName: String, key: String): String? {
        return getSectionAsync(sectionName).getStringKeyAsync(key)
    }

    /**
     * Gets an integer value from a section directly (blocking).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not an integer value or not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getIntValue(sectionName: String, key: String): Int? {
        return getSection(sectionName).getIntKey(key)
    }

    /**
     * Gets an integer value from a section directly (suspendable).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not an integer value or not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getIntValueAsync(sectionName: String, key: String): Int? {
        return getSectionAsync(sectionName).getIntKeyAsync(key)
    }

    /**
     * Gets a float value from a section directly (blocking).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a float value or not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getFloatValue(sectionName: String, key: String): Float? {
        return getSection(sectionName).getFloatKey(key)
    }

    /**
     * Gets a float value from a section directly (suspendable).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a float value or not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getFloatValueAsync(sectionName: String, key: String): Float? {
        return getSectionAsync(sectionName).getFloatKeyAsync(key)
    }

    /**
     * Gets a boolean value from a section directly (blocking).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a boolean value or not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getBooleanValue(sectionName: String, key: String): Boolean? {
        return getSection(sectionName).getBooleanKey(key)
    }

    /**
     * Gets a boolean value from a section directly (suspendable).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a boolean value or not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getBooleanValueAsync(sectionName: String, key: String): Boolean? {
        return getSectionAsync(sectionName).getBooleanKeyAsync(key)
    }

    /**
     * Gets a struct value from a section directly (blocking).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a struct value or not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getStructValue(sectionName: String, key: String): Map<String, Any?> {
        return getSection(sectionName).getStructKey(key)
    }

    /**
     * Gets a struct value from a section directly (suspendable).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a struct value or not a plain value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getStructValueAsync(sectionName: String, key: String): Map<String, Any?> {
        return getSectionAsync(sectionName).getStructKeyAsync(key)
    }

    /**
     * Gets an array value from a section directly (blocking).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not an array value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getArrayValue(sectionName: String, key: String): List<Any?> {
        return getSection(sectionName).getArrayKey(key)
    }

    /**
     * Gets an array value from a section directly (suspendable).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not an array value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getArrayValueAsync(sectionName: String, key: String): List<Any?> {
        return getSectionAsync(sectionName).getArrayKeyAsync(key)
    }

    /**
     * Gets an indexed array value from a section directly (blocking).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not an indexed array value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getIndexedArrayValue(sectionName: String, key: String): Map<Int, Any?> {
        return getSection(sectionName).getIndexedArrayKey(key)
    }

    /**
     * Gets an indexed array value from a section directly (suspendable).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not an indexed array value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getIndexedArrayValueAsync(sectionName: String, key: String): Map<Int, Any?> {
        return getSectionAsync(sectionName).getIndexedArrayKeyAsync(key)
    }

    /**
     * Gets a map value from a section directly (blocking).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a map value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getMapValue(sectionName: String, key: String): Map<String, Any?> {
        return getSection(sectionName).getMapKey(key)
    }

    /**
     * Gets a map value from a section directly (suspendable).
     * @throws NoSuchElementException if the section or key is not found.
     * @throws InvalidTypeException if the key is not a map value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getMapValueAsync(sectionName: String, key: String): Map<String, Any?> {
        return getSectionAsync(sectionName).getMapKeyAsync(key)
    }

    /**
     * Sets a value in a section (blocking). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    fun setValue(sectionName: String, key: String, value: Value) {
        val section = getOrCreateSection(sectionName)
        try {
            section.setKey(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a value in a section (suspendable). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setValueAsync(sectionName: String, key: String, value: Value) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.setKeyAsync(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a string value in a section (blocking). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    fun setValue(sectionName: String, key: String, value: String?) {
        val section = getOrCreateSection(sectionName)
        try {
            section.setKey(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a string value in a section (suspendable). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setValueAsync(sectionName: String, key: String, value: String?) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.setKeyAsync(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets an integer value in a section (blocking). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    fun setValue(sectionName: String, key: String, value: Int?) {
        val section = getOrCreateSection(sectionName)
        try {
            section.setKey(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets an integer value in a section (suspendable). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setValueAsync(sectionName: String, key: String, value: Int?) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.setKeyAsync(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a float value in a section (blocking). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    fun setValue(sectionName: String, key: String, value: Float?) {
        val section = getOrCreateSection(sectionName)
        try {
            section.setKey(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a float value in a section (suspendable). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setValueAsync(sectionName: String, key: String, value: Float?) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.setKeyAsync(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a boolean value in a section (blocking). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    fun setValue(sectionName: String, key: String, value: Boolean?, capitalized: Boolean = true) {
        val section = getOrCreateSection(sectionName)
        try {
            section.setKey(key, value, capitalized)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a boolean value in a section (suspendable). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setValueAsync(sectionName: String, key: String, value: Boolean?, capitalized: Boolean = true) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.setKeyAsync(key, value, capitalized)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a struct value in a section (blocking). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    fun setValue(sectionName: String, key: String, value: Map<String, Any?>) {
        val section = getOrCreateSection(sectionName)
        try {
            section.setKey(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a struct value in a section (suspendable). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a plain value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setValueAsync(sectionName: String, key: String, value: Map<String, Any?>) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.setKeyAsync(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets an array value in a section (blocking). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not an array value.
     */
    @Throws(InvalidTypeException::class)
    fun setArrayValue(sectionName: String, key: String, values: List<Any?>) {
        val section = getOrCreateSection(sectionName)
        try {
            section.setArrayKey(key, values)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set array key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets an array value in a section (suspendable). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not an array value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setArrayValueAsync(sectionName: String, key: String, values: List<Any?>) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.setArrayKeyAsync(key, values)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set array key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets an indexed array value in a section (blocking). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not an indexed array value.
     */
    @Throws(InvalidTypeException::class)
    fun setIndexedArrayValue(sectionName: String, key: String, values: Map<Int, Any?>) {
        val section = getOrCreateSection(sectionName)
        try {
            section.setIndexedArrayKey(key, values)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set indexed array key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets an indexed array value in a section (suspendable). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not an indexed array value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setIndexedArrayValueAsync(sectionName: String, key: String, values: Map<Int, Any?>) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.setIndexedArrayKeyAsync(key, values)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set indexed array key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a map value in a section (blocking). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a map value.
     */
    @Throws(InvalidTypeException::class)
    fun setMapValue(sectionName: String, key: String, value: Map<String, Any?>) {
        val section = getOrCreateSection(sectionName)
        try {
            section.setMapKey(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set map key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Sets a map value in a section (suspendable). Creates the section if it doesn't exist.
     * @throws InvalidTypeException if the key is not a map value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setMapValueAsync(sectionName: String, key: String, value: Map<String, Any?>) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.setMapKeyAsync(key, value)
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to set map key '$key' in section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new key-value pair to a section (blocking). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addValue(sectionName: String, key: String, value: Value) {
        val section = getOrCreateSection(sectionName)
        try {
            section.addKey(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new key-value pair to a section (suspendable). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addValueAsync(sectionName: String, key: String, value: Value) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.addKeyAsync(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new string key-value pair to a section (blocking). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addValue(sectionName: String, key: String, value: String?) {
        val section = getOrCreateSection(sectionName)
        try {
            section.addKey(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new string key-value pair to a section (suspendable). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addValueAsync(sectionName: String, key: String, value: String?) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.addKeyAsync(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new integer key-value pair to a section (blocking). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addValue(sectionName: String, key: String, value: Int?) {
        val section = getOrCreateSection(sectionName)
        try {
            section.addKey(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new integer key-value pair to a section (suspendable). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addValueAsync(sectionName: String, key: String, value: Int?) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.addKeyAsync(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new float key-value pair to a section (blocking). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addValue(sectionName: String, key: String, value: Float?) {
        val section = getOrCreateSection(sectionName)
        try {
            section.addKey(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new float key-value pair to a section (suspendable). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addValueAsync(sectionName: String, key: String, value: Float?) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.addKeyAsync(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new boolean key-value pair to a section (blocking). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addValue(sectionName: String, key: String, value: Boolean?, capitalized: Boolean = true) {
        val section = getOrCreateSection(sectionName)
        try {
            section.addKey(key, value, capitalized)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new boolean key-value pair to a section (suspendable). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addValueAsync(sectionName: String, key: String, value: Boolean?, capitalized: Boolean = true) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.addKeyAsync(key, value, capitalized)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new struct key-value pair to a section (blocking). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addValue(sectionName: String, key: String, value: Map<String, Any?>) {
        val section = getOrCreateSection(sectionName)
        try {
            section.addKey(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new struct key-value pair to a section (suspendable). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addValueAsync(sectionName: String, key: String, value: Map<String, Any?>) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.addKeyAsync(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new array key-value pair to a section (blocking). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addArrayValue(sectionName: String, key: String, values: List<Any?>, type: ArrayType = ArrayType.CommaSeparatedArray) {
        val section = getOrCreateSection(sectionName)
        try {
            section.addArrayKey(key, values, type)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add array key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add array key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new array key-value pair to a section (suspendable). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addArrayValueAsync(sectionName: String, key: String, values: List<Any?>, type: ArrayType = ArrayType.CommaSeparatedArray) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.addArrayKeyAsync(key, values, type)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add array key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add array key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new indexed array key-value pair to a section (blocking). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addIndexedArrayValue(sectionName: String, key: String, values: Map<Int, Any?>) {
        val section = getOrCreateSection(sectionName)
        try {
            section.addIndexedArrayKey(key, values)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add indexed array key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add indexed array key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new indexed array key-value pair to a section (suspendable). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addIndexedArrayValueAsync(sectionName: String, key: String, values: Map<Int, Any?>) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.addIndexedArrayKeyAsync(key, values)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add indexed array key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add indexed array key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new map key-value pair to a section (blocking). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addMapValue(sectionName: String, key: String, value: Map<String, Any?>) {
        val section = getOrCreateSection(sectionName)
        try {
            section.addMapKey(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add map key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add map key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Adds a new map key-value pair to a section (suspendable). Creates the section if it doesn't exist.
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addMapValueAsync(sectionName: String, key: String, value: Map<String, Any?>) {
        val section = getOrCreateSectionAsync(sectionName)
        try {
            section.addMapKeyAsync(key, value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add map key '$key' to section '$sectionName': ${e.message}")
        } catch (e: InvalidTypeException) {
            throw InvalidTypeException("Failed to add map key '$key' to section '$sectionName': ${e.message}")
        }
    }

    /**
     * Deletes a key from a section (blocking).
     * @throws NoSuchElementException if the section or key is not found.
     */
    @Throws(NoSuchElementException::class)
    fun deleteValue(sectionName: String, key: String) {
        val section = getSection(sectionName)
        try {
            section.deleteKey(key)
        } catch (e: NoSuchElementException) {
            throw NoSuchElementException("Failed to delete key '$key' from section '$sectionName': ${e.message}")
        }
    }

    /**
     * Deletes a key from a section (suspendable).
     * @throws NoSuchElementException if the section or key is not found.
     */
    @Throws(NoSuchElementException::class)
    suspend fun deleteValueAsync(sectionName: String, key: String) {
        val section = getSectionAsync(sectionName)
        try {
            section.deleteKeyAsync(key)
        } catch (e: NoSuchElementException) {
            throw NoSuchElementException("Failed to delete key '$key' from section '$sectionName': ${e.message}")
        }
    }

    /**
     * Gets or creates a section with the given name (blocking).
     */
    private fun getOrCreateSection(name: String): Section {
        return rwLock.write {
            _sections.firstOrNull { it.name == name } ?: run {
                val newSection = Section(name)
                _sections.add(newSection)
                newSection
            }
        }
    }

    /**
     * Gets or creates a section with the given name (suspendable).
     */
    private suspend fun getOrCreateSectionAsync(name: String): Section {
        return mutex.withLock {
            _sections.firstOrNull { it.name == name } ?: run {
                val newSection = Section(name)
                _sections.add(newSection)
                newSection
            }
        }
    }
}
