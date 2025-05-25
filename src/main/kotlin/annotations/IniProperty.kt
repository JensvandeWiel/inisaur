package annotations

/**
 * Configures how a property should be serialized in INI format.
 *
 * This annotation allows customization of property serialization behavior
 * for classes marked with [IniSerializable] or [IniStruct].
 *
 * Example:
 * ```kotlin
 * @IniSerializable
 * data class ServerConfig(
 *     @IniProperty(name = "server_name")
 *     val serverName: String,
 *
 *     @IniProperty(ignore = true)
 *     val internalId: String
 * )
 * ```
 *
 * @property name The name to use for this property in the INI file.
 *            If not provided, the property name will be used.
 * @property ignore Whether to ignore this property when serializing/deserializing.
 *
 * @see annotations.IniSerializable
 * @see annotations.IniStruct
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniProperty(
    /**
     * The name to use for this property in the INI file.
     * If not provided, the property name will be used.
     */
    val name: String = "",

    /**
     * Whether to ignore this property when serializing/deserializing.
     */
    val ignore: Boolean = false
)
