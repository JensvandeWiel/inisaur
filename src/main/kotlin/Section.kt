import exceptions.InvalidTypeException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class Section(val name: String) {
    private val _entries: MutableList<Entry> = mutableListOf()
    private val mutex = Mutex()
    private val rwLock = ReentrantReadWriteLock()

    constructor(name: String, entries: List<Entry>?) : this(name) {
        if (entries != null) {
            _entries.addAll(entries)
        }
    }

    override fun toString(): String {
        return rwLock.read { "[$name]\n" + _entries.joinToString("\n") }
    }

    /**
     * Returns all entries in this section. This is a read-only list of entries. And will not modify the original section.
     */
    val entries: List<Entry>
        get() = rwLock.read { _entries.toList() }

    /**
     * Converts a Map<String, Any?> to a StructValue.
     * This function is used to convert a struct-like map into a StructValue.
     * @throws InvalidTypeException if the map contains unsupported value types.
     */
    @Throws(InvalidTypeException::class)
    private fun Map<String, Any?>.toValue(): Map<String, Value> {
        return this.mapValues { (_, value) ->
            when (value) {
                is String -> StringValue(value)
                is Int -> IntValue(value)
                is Float -> FloatValue(value)
                is Boolean -> BoolValue(value)
                is Map<*, *> -> {
                    try {
                        StructValue((value as Map<String, Any?>).toValue())
                    } catch (e: ClassCastException) {
                        throw InvalidTypeException("Invalid struct value: $value")
                    }
                }
                null -> StringValue(null)
                is Value -> value
                else -> throw InvalidTypeException("Unsupported value type: ${value?.javaClass}")
            }
        }
    }

    /**
     * Converts a Map<Int, Any?> to an IndexedArray.
     * This function is used to convert an indexed array-like map into an IndexedArray.
     * @throws InvalidTypeException if the map contains unsupported value types.
     */
    @Throws(InvalidTypeException::class)
    private fun Map<Int, Any?>.toIndexedValue(): Map<Int, Value> {
        return this.mapValues { (_, value) ->
            when (value) {
                is String -> StringValue(value)
                is Int -> IntValue(value)
                is Float -> FloatValue(value)
                is Boolean -> BoolValue(value)
                is Map<*, *> -> {
                    try {
                        StructValue((value as Map<String, Any?>).toValue())
                    } catch (e: ClassCastException) {
                        throw InvalidTypeException("Invalid struct value: $value")
                    }
                }
                is Value -> value
                null -> StringValue(null) // Handle null as a StringValue
                else -> throw InvalidTypeException("Unsupported value type: ${value.javaClass}")
            }
        }
    }

    /**
     * Converts a List<Any?> to a List<Value>.
     * This function is used to convert an array-like list into a List<Value>.
     * @throws InvalidTypeException if the list contains unsupported value types.
     */
    @Throws(InvalidTypeException::class)
    private fun List<Any?>.toValue(): List<Value> {
        return this.map { value ->
            when (value) {
                is String -> StringValue(value)
                is Int -> IntValue(value)
                is Float -> FloatValue(value)
                is Boolean -> BoolValue(value)
                is Map<*, *> -> {
                    try {
                        StructValue((value as Map<String, Any?>).toValue())
                    } catch (e: ClassCastException) {
                        throw InvalidTypeException("Invalid struct value: $value")
                    }
                }
                is Value -> value
                null -> StringValue(null) // Handle null as a StringValue
                else -> throw InvalidTypeException("Unsupported value type: ${value.javaClass}")
            }
        }
    }

    /**
     * Retrieves the value associated with the given key in this section (blocking).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     * */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getKey(key: String): Value {
        return rwLock.read {
            _entries.firstOrNull { it.key == key }?.let {
                when (it) {
                    is Plain -> it.value
                    else -> throw InvalidTypeException("Key '$key' is not a plain value")
                }
            } ?: throw NoSuchElementException("Key '$key' not found in section '$name'")
        }
    }

    /**
     * Retrieves the value associated with the given key in this section (suspendable).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     * */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getKeyAsync(key: String): Value {
        return mutex.withLock {
            _entries.firstOrNull { it.key == key }?.let {
                when (it) {
                    is Plain -> it.value
                    else -> throw InvalidTypeException("Key '$key' is not a plain value")
                }
            } ?: throw NoSuchElementException("Key '$key' not found in section '$name'")
        }
    }

    /**
     * Retrieves the value associated with the given key in this section as a String (blocking).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a string value.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getStringKey(key: String): String? {
        val value = getKey(key)
        if (value is StringValue) {
            return value.value
        }
        throw InvalidTypeException("Key '$key' is not a string value in section '$name'")
    }

    /**
     * Retrieves the value associated with the given key in this section as a String (suspendable).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a string value.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getStringKeyAsync(key: String): String? {
        val value = getKeyAsync(key)
        if (value is StringValue) {
            return value.value
        }
        throw InvalidTypeException("Key '$key' is not a string value in section '$name'")
    }

    /**
     * Retrieves the value associated with the given key in this section as a Integer (blocking).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not an integer value.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getIntKey(key: String): Int? {
        val value = getKey(key)
        if (value is IntValue) {
            return value.value
        }
        throw InvalidTypeException("Key '$key' is not an integer value in section '$name'")
    }

    /**
     * Retrieves the value associated with the given key in this section as a Integer (suspendable).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not an integer value.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getIntKeyAsync(key: String): Int? {
        val value = getKeyAsync(key)
        if (value is IntValue) {
            return value.value
        }
        throw InvalidTypeException("Key '$key' is not an integer value in section '$name'")
    }

    /**
     * Retrieves the value associated with the given key in this section as a Float (blocking).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a float value.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getFloatKey(key: String): Float? {
        val value = getKey(key)
        if (value is FloatValue) {
            return value.value
        }
        throw InvalidTypeException("Key '$key' is not a float value in section '$name'")
    }

    /**
     * Retrieves the value associated with the given key in this section as a Float (suspendable).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a float value.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getFloatKeyAsync(key: String): Float? {
        val value = getKeyAsync(key)
        if (value is FloatValue) {
            return value.value
        }
        throw InvalidTypeException("Key '$key' is not a float value in section '$name'")
    }

    /**
     * Retrieves the value associated with the given key in this section as a Boolean (blocking).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a boolean value.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getBooleanKey(key: String): Boolean? {
        val value = getKey(key)
        if (value is BoolValue) {
            return value.value
        }
        throw InvalidTypeException("Key '$key' is not a boolean value in section '$name'")
    }

    /**
     * Retrieves the value associated with the given key in this section as a Boolean (suspendable).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a boolean value.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getBooleanKeyAsync(key: String): Boolean? {
        val value = getKeyAsync(key)
        if (value is BoolValue) {
            return value.value
        }
        throw InvalidTypeException("Key '$key' is not a boolean value in section '$name'")
    }

    /**
     * Retrieves the value associated with the given key in this section as a Struct (Map<String, Any?>) (blocking).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a struct value.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getStructKey(key: String): Map<String, Any?> {
        val value = getKey(key)
        if (value is StructValue) {
            return value.toMap()
        }
        throw InvalidTypeException("Key '$key' is not a struct value in section '$name'")
    }

    /**
     * Retrieves the value associated with the given key in this section as a Struct (Map<String, Any?>) (suspendable).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a struct value.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getStructKeyAsync(key: String): Map<String, Any?> {
        val value = getKeyAsync(key)
        if (value is StructValue) {
            return value.toMap()
        }
        throw InvalidTypeException("Key '$key' is not a struct value in section '$name'")
    }

    /**
     * Retrieves the value associated with the given key in this section as a List<Any?> (blocking).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not an array value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getArrayKey(key: String): List<Any?> {
        return rwLock.read {
            _entries.firstOrNull { it.key == key }?.let {
                when (it) {
                    is CommaSeparatedArray -> it.toList()
                    is RepeatedLineArray -> it.toList()
                    else -> throw InvalidTypeException("Key '$key' is not an array value")
                }
            } ?: throw NoSuchElementException("Key '$key' not found in section '$name'")
        }
    }

    /**
     * Retrieves the value associated with the given key in this section as a List<Any?> (suspendable).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not an array value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getArrayKeyAsync(key: String): List<Any?> {
        return mutex.withLock {
            _entries.firstOrNull { it.key == key }?.let {
                when (it) {
                    is CommaSeparatedArray -> it.toList()
                    is RepeatedLineArray -> it.toList()
                    else -> throw InvalidTypeException("Key '$key' is not an array value")
                }
            } ?: throw NoSuchElementException("Key '$key' not found in section '$name'")
        }
    }

    /**
     * Retrieves the value associated with the given key in this section as a Map<Int, Any?> (blocking).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not an indexed array value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getIndexedArrayKey(key: String): Map<Int, Any?> {
        return rwLock.read {
            _entries.firstOrNull { it.key == key }?.let {
                when (it) {
                    is IndexedArray -> it.toMap()
                    else -> throw InvalidTypeException("Key '$key' is not an indexed array value")
                }
            } ?: throw NoSuchElementException("Key '$key' not found in section '$name'")
        }
    }

    /**
     * Retrieves the value associated with the given key in this section as a Map<Int, Any?> (suspendable).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not an indexed array value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getIndexedArrayKeyAsync(key: String): Map<Int, Any?> {
        return mutex.withLock {
            _entries.firstOrNull { it.key == key }?.let {
                when (it) {
                    is IndexedArray -> it.toMap()
                    else -> throw InvalidTypeException("Key '$key' is not an indexed array value")
                }
            } ?: throw NoSuchElementException("Key '$key' not found in section '$name'")
        }
    }

    /**
     * Retrieves the value associated with the given key in this section as a Map<String, Any?> (blocking).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a map value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    fun getMapKey(key: String): Map<String, Any?> {
        return rwLock.read {
            _entries.firstOrNull { it.key == key }?.let {
                when (it) {
                    is MapEntry -> it.toMap()
                    else -> throw InvalidTypeException("Key '$key' is not a map value")
                }
            } ?: throw NoSuchElementException("Key '$key' not found in section '$name'")
        }
    }

    /**
     * Retrieves the value associated with the given key in this section as a Map<String, Any?> (suspendable).
     * @throws NoSuchElementException if the key is not found.
     * @throws InvalidTypeException if the key is not a map value.
     */
    @Throws(NoSuchElementException::class, InvalidTypeException::class)
    suspend fun getMapKeyAsync(key: String): Map<String, Any?> {
        return mutex.withLock {
            _entries.firstOrNull { it.key == key }?.let {
                when (it) {
                    is MapEntry -> it.toMap()
                    else -> throw InvalidTypeException("Key '$key' is not a map value")
                }
            } ?: throw NoSuchElementException("Key '$key' not found in section '$name'")
        }
    }

    /**
     * Sets the value for the given key in this section (blocking).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    fun setKey(key: String, value: Value) {
        rwLock.write {
            val existingEntry = _entries.firstOrNull { it.key == key }
            if (existingEntry != null) {
                if (existingEntry is Plain) {
                    _entries[_entries.indexOf(existingEntry)] = Plain(key, value)
                } else {
                    throw InvalidTypeException("Key '$key' is not a plain value")
                }
            } else {
                _entries += Plain(key, value)
            }
        }
    }

    /**
     * Sets the value for the given key in this section (suspendable).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setKeyAsync(key: String, value: Value) {
        mutex.withLock {
            val existingEntry = _entries.firstOrNull { it.key == key }
            if (existingEntry != null) {
                if (existingEntry is Plain) {
                    _entries[_entries.indexOf(existingEntry)] = Plain(key, value)
                } else {
                    throw InvalidTypeException("Key '$key' is not a plain value")
                }
            } else {
                _entries += Plain(key, value)
            }
        }
    }

    /**
     * Sets the value for the given key in this section (blocking).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    fun setKey(key: String, value: String?) {
        setKey(key, StringValue(value))
    }

    /**
     * Sets the value for the given key in this section (suspendable).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setKeyAsync(key: String, value: String?) {
        setKeyAsync(key, StringValue(value))
    }

    /**
     * Sets the value for the given key in this section (blocking).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    fun setKey(key: String, value: Int?) {
        setKey(key, IntValue(value))
    }

    /**
     * Sets the value for the given key in this section (suspendable).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setKeyAsync(key: String, value: Int?) {
        setKeyAsync(key, IntValue(value))
    }

    /**
     * Sets the value for the given key in this section (blocking).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    fun setKey(key: String, value: Float?) {
        setKey(key, FloatValue(value))
    }

    /**
     * Sets the value for the given key in this section (suspendable).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setKeyAsync(key: String, value: Float?) {
        setKeyAsync(key, FloatValue(value))
    }

    /**
     * Sets the value for the given key in this section (blocking).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    fun setKey(key: String, value: Boolean?, capitalized: Boolean = true) {
        setKey(key, BoolValue(value, capitalized))
    }

    /**
     * Sets the value for the given key in this section (suspendable).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setKeyAsync(key: String, value: Boolean?, capitalized: Boolean = true) {
        setKeyAsync(key, BoolValue(value, capitalized))
    }

    /**
     * Sets the value for the given key in this section (blocking).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    fun setKey(key: String, value: Map<String, Any?>) {
        setKey(key, StructValue(value.toValue()))
    }

    /**
     * Sets the value for the given key in this section (suspendable).
     * If the key already exists and is a plain value, it updates the value.
     * If the key does not exist, it adds a new plain entry.
     * @throws InvalidTypeException if the key is not a plain (not an array) value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setKeyAsync(key: String, value: Map<String, Any?>) {
        setKeyAsync(key, StructValue(value.toValue()))
    }

    /**
     * Sets the value for the given key in this section (blocking).
     * If the key already exists and is an array value, it updates the value.
     * If the key does not exist, it adds a new array entry (CommaSeparatedArray).
     * @throws InvalidTypeException if the key is not an array value.
     */
    @Throws(InvalidTypeException::class)
    fun setArrayKey(key: String, values: List<Any?>) {
        rwLock.write {
            val existingEntry = _entries.firstOrNull { it.key == key }
            when (existingEntry) {
                is CommaSeparatedArray -> {
                    _entries[_entries.indexOf(existingEntry)] = CommaSeparatedArray(key, values.toValue())
                }
                is RepeatedLineArray -> {
                    _entries[_entries.indexOf(existingEntry)] = RepeatedLineArray(key, values.toValue())
                }
                null -> {
                    _entries += CommaSeparatedArray(key, values.toValue())
                }
                else -> throw InvalidTypeException("Key '$key' is not an array value")
            }
        }
    }

    /**
     * Sets the value for the given key in this section (suspendable).
     * If the key already exists and is an array value, it updates the value.
     * If the key does not exist, it adds a new array entry (CommaSeparatedArray).
     * @throws InvalidTypeException if the key is not an array value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setArrayKeyAsync(key: String, values: List<Any?>) {
        mutex.withLock {
            val existingEntry = _entries.firstOrNull { it.key == key }
            when (existingEntry) {
                is CommaSeparatedArray -> {
                    _entries[_entries.indexOf(existingEntry)] = CommaSeparatedArray(key, values.toValue())
                }
                is RepeatedLineArray -> {
                    _entries[_entries.indexOf(existingEntry)] = RepeatedLineArray(key, values.toValue())
                }
                null -> {
                    _entries += CommaSeparatedArray(key, values.toValue())
                }
                else -> throw InvalidTypeException("Key '$key' is not an array value")
            }
        }
    }

    /**
     * Sets the value for the given key in this section (blocking).
     * If the key already exists and is an indexed array value, it updates the value.
     * If the key does not exist, it adds a new indexed array entry.
     * @throws InvalidTypeException if the key is not an indexed array value.
     */
    @Throws(InvalidTypeException::class)
    fun setIndexedArrayKey(key: String, values: Map<Int, Any?>) {
        rwLock.write {
            val existingEntry = _entries.firstOrNull { it.key == key }
            when (existingEntry) {
                is IndexedArray -> {
                    _entries[_entries.indexOf(existingEntry)] = IndexedArray(key, values.toIndexedValue())
                }
                null -> {
                    _entries += IndexedArray(key, values.toIndexedValue())
                }
                else -> throw InvalidTypeException("Key '$key' is not an indexed array value")
            }
        }
    }

    /**
     * Sets the value for the given key in this section (suspendable).
     * If the key already exists and is an indexed array value, it updates the value.
     * If the key does not exist, it adds a new indexed array entry.
     * @throws InvalidTypeException if the key is not an indexed array value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setIndexedArrayKeyAsync(key: String, values: Map<Int, Any?>) {
        mutex.withLock {
            val existingEntry = _entries.firstOrNull { it.key == key }
            when (existingEntry) {
                is IndexedArray -> {
                    _entries[_entries.indexOf(existingEntry)] = IndexedArray(key, values.toIndexedValue())
                }
                null -> {
                    _entries += IndexedArray(key, values.toIndexedValue())
                }
                else -> throw InvalidTypeException("Key '$key' is not an indexed array value")
            }
        }
    }

    /**
     * Sets the value for the given key in this section (blocking).
     * If the key already exists and is a map value, it updates the value.
     * If the key does not exist, it adds a new map entry.
     * @throws InvalidTypeException if the key is not a map value.
     */
    @Throws(InvalidTypeException::class)
    fun setMapKey(key: String, value: Map<String, Any?>) {
        rwLock.write {
            val existingEntry = _entries.firstOrNull { it.key == key }
            when (existingEntry) {
                is MapEntry -> {
                    _entries[_entries.indexOf(existingEntry)] = MapEntry(key, value.toValue())
                }
                null -> {
                    _entries += MapEntry(key, value.toValue())
                }
                else -> throw InvalidTypeException("Key '$key' is not a map value")
            }
        }
    }

    /**
     * Sets the value for the given key in this section (suspendable).
     * If the key already exists and is a map value, it updates the value.
     * If the key does not exist, it adds a new map entry.
     * @throws InvalidTypeException if the key is not a map value.
     */
    @Throws(InvalidTypeException::class)
    suspend fun setMapKeyAsync(key: String, value: Map<String, Any?>) {
        mutex.withLock {
            val existingEntry = _entries.firstOrNull { it.key == key }
            when (existingEntry) {
                is MapEntry -> {
                    _entries[_entries.indexOf(existingEntry)] = MapEntry(key, value.toValue())
                }
                null -> {
                    _entries += MapEntry(key, value.toValue())
                }
                else -> throw InvalidTypeException("Key '$key' is not a map value")
            }
        }
    }

    /**
     * Adds a new entry to this section (blocking).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addKey(key: String, value: Value) {
        rwLock.write {
            if (_entries.any { it.key == key }) {
                throw IllegalArgumentException("Key '$key' already exists in section '$name'")
            }
            _entries.add(Plain(key, value))
        }
    }

    /**
     * Adds a new entry to this section (suspendable).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addKeyAsync(key: String, value: Value) {
        mutex.withLock {
            if (_entries.any { it.key == key }) {
                throw IllegalArgumentException("Key '$key' already exists in section '$name'")
            }
            _entries.add(Plain(key, value))
        }
    }

    /**
     * Adds a new entry to this section (blocking).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addKey(key: String, value: String?) {
        addKey(key, StringValue(value))
    }

    /**
     * Adds a new entry to this section (suspendable).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addKeyAsync(key: String, value: String?) {
        addKeyAsync(key, StringValue(value))
    }

    /**
     * Adds a new entry to this section (blocking).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addKey(key: String, value: Int?) {
        addKey(key, IntValue(value))
    }

    /**
     * Adds a new entry to this section (suspendable).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addKeyAsync(key: String, value: Int?) {
        addKeyAsync(key, IntValue(value))
    }

    /**
     * Adds a new entry to this section (blocking).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addKey(key: String, value: Float?) {
        addKey(key, FloatValue(value))
    }

    /**
     * Adds a new entry to this section (suspendable).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addKeyAsync(key: String, value: Float?) {
        addKeyAsync(key, FloatValue(value))
    }

    /**
     * Adds a new entry to this section (blocking).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addKey(key: String, value: Boolean?, capitalized: Boolean = true) {
        addKey(key, BoolValue(value, capitalized))
    }

    /**
     * Adds a new entry to this section (suspendable).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addKeyAsync(key: String, value: Boolean?, capitalized: Boolean = true) {
        addKeyAsync(key, BoolValue(value, capitalized))
    }

    /**
     * Adds a new entry to this section (blocking).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addKey(key: String, value: Map<String, Any?>) {
        addKey(key, StructValue(value.toValue()))
    }

    /**
     * Adds a new entry to this section (suspendable).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addKeyAsync(key: String, value: Map<String, Any?>) {
        addKeyAsync(key, StructValue(value.toValue()))
    }

    /**
     * Adds a new entry to this section (blocking).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addArrayKey(key: String, values: List<Any?>, type: ArrayType = ArrayType.CommaSeparatedArray) {
        rwLock.write {
            if (_entries.any { it.key == key }) {
                throw IllegalArgumentException("Key '$key' already exists in section '$name'")
            }
            when (type) {
                ArrayType.CommaSeparatedArray -> _entries.add(CommaSeparatedArray(key, values.toValue()))
                ArrayType.RepeatedLineArray -> _entries.add(RepeatedLineArray(key, values.toValue()))
            }
        }
    }

    /**
     * Adds a new entry to this section (suspendable).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addArrayKeyAsync(key: String, values: List<Any?>, type: ArrayType = ArrayType.CommaSeparatedArray) {
        mutex.withLock {
            if (_entries.any { it.key == key }) {
                throw IllegalArgumentException("Key '$key' already exists in section '$name'")
            }
            when (type) {
                ArrayType.CommaSeparatedArray -> _entries.add(CommaSeparatedArray(key, values.toValue()))
                ArrayType.RepeatedLineArray -> _entries.add(RepeatedLineArray(key, values.toValue()))
            }
        }
    }

    /**
     * Adds a new entry to this section (blocking).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addIndexedArrayKey(key: String, values: Map<Int, Any?>) {
        rwLock.write {
            if (_entries.any { it.key == key }) {
                throw IllegalArgumentException("Key '$key' already exists in section '$name'")
            }
            _entries.add(IndexedArray(key, values.toIndexedValue()))
        }
    }

    /**
     * Adds a new entry to this section (suspendable).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addIndexedArrayKeyAsync(key: String, values: Map<Int, Any?>) {
        mutex.withLock {
            if (_entries.any { it.key == key }) {
                throw IllegalArgumentException("Key '$key' already exists in section '$name'")
            }
            _entries.add(IndexedArray(key, values.toIndexedValue()))
        }
    }

    /**
     * Adds a new entry to this section (blocking).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    fun addMapKey(key: String, value: Map<String, Any?>) {
        rwLock.write {
            if (_entries.any { it.key == key }) {
                throw IllegalArgumentException("Key '$key' already exists in section '$name'")
            }
            _entries.add(MapEntry(key, value.toValue()))
        }
    }

    /**
     * Adds a new entry to this section (suspendable).
     * @throws IllegalArgumentException if the key already exists in the section.
     * @throws InvalidTypeException if the entry is not a valid type.
     */
    @Throws(IllegalArgumentException::class, InvalidTypeException::class)
    suspend fun addMapKeyAsync(key: String, value: Map<String, Any?>) {
        mutex.withLock {
            if (_entries.any { it.key == key }) {
                throw IllegalArgumentException("Key '$key' already exists in section '$name'")
            }
            _entries.add(MapEntry(key, value.toValue()))
        }
    }

    /**
     * Removes the entry with the specified key from this section (blocking).
     * @throws NoSuchElementException if the key is not found.
     */
    @Throws(NoSuchElementException::class)
    fun deleteKey(key: String) {
        rwLock.write {
            val entry = _entries.firstOrNull { it.key == key }
            if (entry != null) {
                _entries.remove(entry)
            } else {
                throw NoSuchElementException("Key '$key' not found in section '$name'")
            }
        }
    }

    /**
     * Removes the entry with the specified key from this section (suspendable).
     * @throws NoSuchElementException if the key is not found.
     */
    @Throws(NoSuchElementException::class)
    suspend fun deleteKeyAsync(key: String) {
        mutex.withLock {
            val entry = _entries.firstOrNull { it.key == key }
            if (entry != null) {
                _entries.remove(entry)
            } else {
                throw NoSuchElementException("Key '$key' not found in section '$name'")
            }
        }
    }
}
