package annotations

/**
 * Marks a class as serializable to INI format.
 *
 * Classes with this annotation can be converted to INI sections. Each property of the class
 * will be serialized as a key-value pair within the section, unless otherwise configured.
 *
 * Example:
 * ```kotlin
 * @IniSerializable("ServerSettings")
 * data class ServerConfig(
 *     val serverName: String,
 *     val maxPlayers: Int,
 *     val enablePvP: Boolean
 * )
 * ```
 *
 * Will produce:
 * ```ini
 * [ServerSettings]
 * serverName=MyServer
 * maxPlayers=50
 * enablePvP=True
 * ```
 *
 * @property sectionName The name of the section in the INI file. If not provided, the simple name of the class will be used.
 *
 * @see annotations.IniProperty
 * @see annotations.IniBoolean
 * @see annotations.IniArray
 * @see annotations.IniStruct
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniSerializable(
    /**
     * The name of the section in the INI file.
     * If not provided, the simple name of the class will be used.
     */
    val sectionName: String = ""
)
