import enums.IniEntryType
import enums.IniValueType
import exceptions.InvalidTypeException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class IniEntry {
    val key: String
    private var _value: Any?
    val type: IniEntryType
    private val mutex = Mutex()

    private fun handleNewValue(value: Any, type: IniEntryType): Any {
        return when (value) {
            is IniValue -> value
            is List<*>, is MutableList<*> -> {
                if (type != IniEntryType.CommaSeparatedArray && type != IniEntryType.RepeatedLineArray && type != IniEntryType.IndexedArray) {
                    throw InvalidTypeException("Invalid type for IniEntry: $type")
                }
                return value.map { v ->
                    when (v) {
                        is IniValue -> v
                        is String -> IniValue(v)
                        is Boolean -> IniValue(v)
                        is Int -> IniValue(v)
                        is Float -> IniValue(v)
                        null -> IniValue(null as String?)
                        else -> throw InvalidTypeException("Invalid type for IniEntry: ${v?.javaClass}")
                    }
                }.toMutableList()
            }
            is Map<*, *>, is MutableMap<*, *> -> {
                if (type != IniEntryType.Map) {
                    throw InvalidTypeException("Invalid type for IniEntry: $type")
                }

                return value.mapValues { entry ->
                    when (val v = entry.value) {
                        is IniValue -> v
                        is String -> IniValue(v)
                        is Boolean -> IniValue(v)
                        is Int -> IniValue(v)
                        is Float -> IniValue(v)
                        null -> IniValue(null as String?)
                        else -> throw InvalidTypeException("Invalid type for IniEntry: ${v?.javaClass}")
                    }
                }.toMutableMap()
            }
            else -> throw InvalidTypeException("Invalid type for IniEntry: ${value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    constructor(
        key: String,
        value: Any,
        type: IniEntryType,
    ) {
        this.key = key
        this._value = handleNewValue(value, type)
        this.type = type

        if (key.isEmpty()) {
            throw InvalidTypeException("Key cannot be empty")
        }
    }

    @Throws(InvalidTypeException::class)
    constructor(key: String, value: IniValue) : this(
        key,
        value,
        IniEntryType.Plain
    )

    @Throws(InvalidTypeException::class)
    constructor(key: String, value: List<IniValue>, type: IniEntryType) : this(
        key,
        value as Any,
        type
    )

    @Throws(InvalidTypeException::class)
    constructor(key: String, value: Map<String, IniValue>) : this(
        key,
        value,
        IniEntryType.Map
    )

    private fun keyString(value: String): String {
        return "$key=$value"
    }

    @Throws(InvalidTypeException::class)
    override fun toString(): String {
        return runBlocking {
            when (type) {
                IniEntryType.Plain -> toStringValue()
                IniEntryType.CommaSeparatedArray -> toCommaSeparatedArrayString()
                IniEntryType.RepeatedLineArray -> toRepeatedLineArrayString()
                IniEntryType.IndexedArray -> toIndexedArrayString()
                IniEntryType.Map -> toMutableMapString()
            }
        }
    }

    @Throws(InvalidTypeException::class)
    private suspend fun toStringValue(): String = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }
        return keyString(when (_value) {
            is IniValue -> _value.toString()
            null -> ""
            else -> throw InvalidTypeException("Invalid type for Plain: ${_value?.javaClass}")
        })
    }

    @Throws(InvalidTypeException::class)
    private suspend fun toCommaSeparatedArrayString(): String = mutex.withLock {
        if (type != IniEntryType.CommaSeparatedArray) {
            throw InvalidTypeException("Invalid type for CommaSeparatedArray: $type")
        }
        return keyString(when (_value) {
            is MutableList<*> -> (_value as MutableList<*>)
                .filter {
                    it != null && (it !is IniValue || it.getValue() != null)
                }
                .joinToString(",")
            null -> ""
            else -> throw InvalidTypeException("Invalid type for CommaSeparatedArray: ${_value?.javaClass}")
        })
    }

    @Throws(InvalidTypeException::class)
    private suspend fun toRepeatedLineArrayString(): String = mutex.withLock {
        if (type != IniEntryType.RepeatedLineArray) {
            throw InvalidTypeException("Invalid type for RepeatedLineArray: $type")
        }
        return when (_value) {
            is MutableList<*> -> (_value as MutableList<*>)
                .filter {
                    it != null && (it !is IniValue || it.getValue() != null)
                }
                .joinToString("\n") { "$key=$it" }
            null -> ""
            else -> throw InvalidTypeException("Invalid type for RepeatedLineArray: ${_value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    private suspend fun toIndexedArrayString(): String = mutex.withLock {
        if (type != IniEntryType.IndexedArray) {
            throw InvalidTypeException("Invalid type for IndexedArray: $type")
        }
        val parentKey = key
        return when (_value) {
            is MutableList<*> -> {
                val list = _value as MutableList<*>
                val sb = StringBuilder()
                list.forEachIndexed { idx, v ->
                    sb.append("$parentKey[$idx]=")
                    if (v is IniValue && v.getValue() != null) {
                        sb.append(v.toString())
                    }
                    sb.append("\n")
                }
                if (sb.isNotEmpty()) sb.setLength(sb.length - 1)
                sb.toString()
            }
            null -> ""
            else -> throw InvalidTypeException("Invalid type for IndexedArray: ${_value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    private suspend fun toMutableMapString(): String = mutex.withLock {
        if (type != IniEntryType.Map) {
            throw InvalidTypeException("Invalid type for MutableMap: $type")
        }
        return when (_value) {
            is MutableMap<*, *> -> (_value as MutableMap<*, *>).entries.joinToString("\n") { "$key[${it.key}]=${it.value}" }
            null -> ""
            else -> throw InvalidTypeException("Invalid type for MutableMap: ${_value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    suspend fun getValue(): IniValue = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }
        return _value as IniValue
    }

    @Throws(InvalidTypeException::class)
    suspend fun getBoolean(): Boolean? = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }

        if (_value is IniValue && (_value as IniValue).type() == IniValueType.Boolean || (_value as IniValue).type() == IniValueType.CapitalizedBoolean) {
            return (_value as IniValue).getBoolean()
        } else {
            throw InvalidTypeException("Invalid type for Plain: ${_value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    suspend fun getInteger(): Int? = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }

        if (_value is IniValue && (_value as IniValue).type() == IniValueType.Integer) {
            return (_value as IniValue).getInteger()
        } else {
            throw InvalidTypeException("Invalid type for Plain: ${_value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    suspend fun getFloat(): Float? = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }

        if (_value is IniValue && (_value as IniValue).type() == IniValueType.Float) {
            return (_value as IniValue).getFloat()
        } else {
            throw InvalidTypeException("Invalid type for Plain: ${_value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    suspend fun getString(): String? = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }

        if (_value is IniValue && (_value as IniValue).type() == IniValueType.String) {
            return (_value as IniValue).getString()
        } else {
            throw InvalidTypeException("Invalid type for Plain: ${_value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    suspend fun getStruct(): Struct? = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }

        if (_value is IniValue && (_value as IniValue).type() == IniValueType.Struct) {
            return (_value as IniValue).getStruct()
        } else {
            throw InvalidTypeException("Invalid type for Plain: ${_value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    suspend fun setValue(value: IniValue) = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }
        _value = handleNewValue(value, type)
    }

    @Throws(InvalidTypeException::class)
    suspend fun setBoolean(value: Boolean?) = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }
        _value = handleNewValue(IniValue(value,isCapitalizedBoolean(value)), type)
    }

    @Throws(InvalidTypeException::class)
    suspend fun setInteger(value: Int?) = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }
        _value = handleNewValue(IniValue(value), type)
    }

    @Throws(InvalidTypeException::class)
    suspend fun setFloat(value: Float?) = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }
        _value = handleNewValue(IniValue(value), type)
    }

    @Throws(InvalidTypeException::class)
    suspend fun setString(value: String?) = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }
        _value = handleNewValue(IniValue(value), type)
    }

    @Throws(InvalidTypeException::class)
    suspend fun setStruct(value: Struct?) = mutex.withLock {
        if (type != IniEntryType.Plain) {
            throw InvalidTypeException("Invalid type for Plain: $type")
        }
        _value = handleNewValue(IniValue(value), type)
    }

    @Throws(InvalidTypeException::class)
    suspend fun getArrayValues(): List<Any?> = mutex.withLock {
        if (type != IniEntryType.CommaSeparatedArray && type != IniEntryType.RepeatedLineArray && type != IniEntryType.IndexedArray) {
            throw InvalidTypeException("Invalid type for array: $type")
        }
        return when (_value) {
            is MutableList<*> -> (_value as MutableList<*>)
                .map {
                    if (it is IniValue) {
                        it.getValue()
                    } else {
                        it
                    }
                }
            null -> emptyList()
            else -> throw InvalidTypeException("Invalid type for array: ${_value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    suspend fun setArrayValues(value: List<IniValue>) = mutex.withLock {
        if (type != IniEntryType.CommaSeparatedArray && type != IniEntryType.RepeatedLineArray && type != IniEntryType.IndexedArray) {
            throw InvalidTypeException("Invalid type for array: $type")
        }
        _value = handleNewValue(value as Any, type)
    }

    @Throws(InvalidTypeException::class)
    suspend fun getMapValues(): Map<String, Any?>? = mutex.withLock {
        if (type != IniEntryType.Map) {
            throw InvalidTypeException("Invalid type for Map: $type")
        }

        return when (_value) {
            is MutableMap<*, *> -> (_value as MutableMap<String, Any?>)
                .mapValues {
                    if (it.value is IniValue) {
                        (it.value as IniValue).getValue()
                    } else {
                        it.value
                    }
                }

            null -> null
            else -> throw InvalidTypeException("Invalid type for array: ${_value?.javaClass}")
        }
    }

    @Throws(InvalidTypeException::class)
    suspend fun setMapValues(value: Map<String, IniValue>) = mutex.withLock {
        if (type != IniEntryType.Map) {
            throw InvalidTypeException("Invalid type for Map: $type")
        }
        _value = handleNewValue(value as Any, type)
    }

    private fun isCapitalizedBoolean(value: Any?): Boolean {
        return when (value) {
            is IniValue -> value.type() == IniValueType.CapitalizedBoolean
            is Boolean -> true
            else -> false
        }
    }

    fun entryType(): IniEntryType {
        return type
    }

    fun plainEntryValueType(): IniValueType {
        return when (type) {
            IniEntryType.Plain -> {
                if (_value is IniValue) {
                    (_value as IniValue).type()
                } else {
                    throw InvalidTypeException("Invalid type for Plain: ${_value?.javaClass}")
                }
            }
            else -> throw InvalidTypeException("Invalid type for Plain: $type")
        }
    }
}