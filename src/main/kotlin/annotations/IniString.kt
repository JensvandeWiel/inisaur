package annotations

/**
 * Configures how a String property should be serialized in INI format.
 *
 * This annotation allows customization of string value representation in INI files,
 * specifically whether the value should be enclosed in quotes.
 *
 * Example:
 * ```kotlin
 * @IniSerializable
 * data class ServerConfig(
 *     @IniString(quoted = true)
 *     val serverName: String,
 *
 *     @IniString(quoted = false)
 *     val description: String
 * )
 * ```
 *
 * Will produce:
 * ```ini
 * [ServerConfig]
 * serverName="My Server"
 * description=This is a description
 * ```
 *
 * @property quoted Whether the string value should be enclosed in quotes.
 * @see annotations.IniSerializable
 * @see annotations.IniProperty
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniString(
    /**
     * Whether the string value should be enclosed in quotes.
     * Default is false.
     */
    val quoted: Boolean = false
)
