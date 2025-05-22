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
            is Map<*, *> /* Struct */ -> value.entries.joinToString(", ", "(", ")") { "${it.key}=${it.value}" }
            null -> ""
            else -> throw InvalidTypeException("Invalid type for Struct: ${value::class.java}")
        }
    }
}