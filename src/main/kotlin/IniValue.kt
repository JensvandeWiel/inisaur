import enums.IniEntryType
import enums.IniValueType
import exceptions.InvalidTypeException


class IniValue(
    private val value: Any?,
    private val type: IniValueType
) {

    constructor(value: String?) : this(value, IniValueType.String)
    constructor(value: Boolean?, capitalized: Boolean = true) : this(
        value,
        if (capitalized) IniValueType.CapitalizedBoolean else IniValueType.Boolean
    )
    constructor(value: Int?) : this(value, IniValueType.Integer)
    constructor(value: Float?) : this(value, IniValueType.Float)
    constructor(value: Struct?) : this(value, IniValueType.Struct)

    override fun toString(): String {
        return when (type) {
            IniValueType.CapitalizedBoolean -> toCapitalizedBooleanString()
            IniValueType.Boolean -> toBooleanString()
            IniValueType.Integer -> toIntegerString()
            IniValueType.Float -> toFloatString()
            IniValueType.String -> toStringValue()
            IniValueType.Struct -> toStructString()
        }
    }

    private fun toCapitalizedBooleanString(): String {
        return when (value) {
            is Boolean -> value.toString().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            null -> ""
            else -> throw InvalidTypeException("Invalid type for CapitalizedBoolean: ${value::class.java}")
        }
    }

    @Throws(InvalidTypeException::class)
    fun toBooleanString(): String {
        return when (value) {
            is Boolean -> value.toString()
            null -> ""
            else -> throw InvalidTypeException("Invalid type for Boolean: ${value::class.java}")
        }
    }

    @Throws(InvalidTypeException::class)
    fun toIntegerString(): String {
        return when (value) {
            is Int -> value.toString()
            null -> ""
            else -> throw InvalidTypeException("Invalid type for Integer: ${value::class.java}")
        }
    }

    @Throws(InvalidTypeException::class)
    fun toFloatString(): String {
        return when (value) {
            is Float -> value.toString()
            null -> ""
            else -> throw InvalidTypeException("Invalid type for Float: ${value::class.java}")
        }
    }

    @Throws(InvalidTypeException::class)
    fun toStringValue(): String {
        return when (value) {
            is String -> value
            null -> ""
            else -> throw InvalidTypeException("Invalid type for String: ${value::class.java}")
        }
    }

    @Throws(InvalidTypeException::class)
    fun toStructString(): String {
        return when (value) {
            is Map<*, *> /* Struct */ -> value.entries.joinToString(", ", "(", ")") { "${it.key}=${if (it.value != null) it.value.toString() else ""}" }
            null -> ""
            else -> throw InvalidTypeException("Invalid type for Struct: ${value::class.java}")
        }
    }

    @Throws(InvalidTypeException::class)
    fun getValue(): Any? {
        return when (type) {
            IniValueType.CapitalizedBoolean, IniValueType.Boolean -> getBoolean()
            IniValueType.Integer -> getInteger()
            IniValueType.Float -> getFloat()
            IniValueType.String -> getString()
            IniValueType.Struct -> getStruct()
        }
    }

    @Throws(InvalidTypeException::class)
    fun getBoolean(): Boolean? {
        return when (type) {
            IniValueType.CapitalizedBoolean, IniValueType.Boolean -> value as? Boolean
            else -> throw InvalidTypeException("Invalid type for Boolean: $type")
        }
    }

    @Throws(InvalidTypeException::class)
    fun getInteger(): Int? {
        return when (type) {
            IniValueType.Integer -> value as? Int
            else -> throw InvalidTypeException("Invalid type for Integer: $type")
        }
    }

    @Throws(InvalidTypeException::class)
    fun getFloat(): Float? {
        return when (type) {
            IniValueType.Float -> value as? Float
            else -> throw InvalidTypeException("Invalid type for Float: $type")
        }
    }

    @Throws(InvalidTypeException::class)
    fun getString(): String? {
        return when (type) {
            IniValueType.String -> value as? String
            else -> throw InvalidTypeException("Invalid type for String: $type")
        }
    }

    @Throws(InvalidTypeException::class)
    fun getStruct(): Map<String, Any?>? {
        return when (value) {
            is MutableMap<*, *> -> (value as MutableMap<String, Any?>)
                .mapValues {
                    if (it.value is IniValue) {
                        (it.value as IniValue).getValue()
                    } else {
                        it.value
                    }
                }

            null -> null
            else -> throw InvalidTypeException("Invalid type for array: ${value?.javaClass}")
        }
    }

    fun type(): IniValueType {
        return type
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) return false
        other as IniValue
        if (value != other.value) return false
        if (type != other.type) return false
        return true
    }
}