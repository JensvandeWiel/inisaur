package annotations

/**
 * Marks a class as an INI struct value.
 *
 * Classes with this annotation can be serialized as struct values within INI files.
 * Struct values are represented as nested key-value pairs with parentheses in INI format:
 * ```
 * key=(field1=value1, field2=value2)
 * ```
 *
 * @see annotations.IniSerializable
 * @see annotations.IniProperty
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniStruct
