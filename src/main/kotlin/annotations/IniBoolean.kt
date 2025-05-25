package annotations

/**
 * Configures how a Boolean property should be serialized in INI format.
 *
 * This annotation allows customization of boolean value representation in INI files,
 * with the option to use capitalized (True/False) or lowercase (true/false) formats.
 *
 * Example:
 * ```kotlin
 * @IniSerializable
 * data class ServerConfig(
 *     @IniBoolean(capitalized = true)
 *     val enablePvP: Boolean,
 *
 *     @IniBoolean(capitalized = false)
 *     val allowBuildingDamage: Boolean
 * )
 * ```
 *
 * Will produce:
 * ```ini
 * [ServerConfig]
 * enablePvP=True
 * allowBuildingDamage=true
 * ```
 *
 * @property capitalized Whether the boolean value should be capitalized (True/False) or lowercase (true/false).
 * @see annotations.IniSerializable
 * @see annotations.IniProperty
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniBoolean(
    /**
     * Whether the boolean value should be capitalized (True/False) or lowercase (true/false).
     * Default is true (capitalized).
     */
    val capitalized: Boolean = true
)
