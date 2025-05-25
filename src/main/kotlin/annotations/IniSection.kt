package annotations

/**
 * Marks a class as an INI section or a property as containing an INI section.
 *
 * When applied to classes:
 * Classes with this annotation can be converted to INI sections. Each property of the class
 * will be serialized as a key-value pair within the section, unless otherwise configured.
 *
 * When applied to properties:
 * Properties with this annotation represent sections within a class annotated with [IniSerializable].
 * The property type should be a class annotated with @IniSection.
 *
 * Example for class usage:
 * ```kotlin
 * @IniSection("ServerSettings")
 * data class ServerConfig(
 *     val serverName: String,
 *     val maxPlayers: Int,
 *     val enablePvP: Boolean
 * )
 * ```
 *
 * Example for property usage:
 * ```kotlin
 * @IniSerializable
 * data class GameConfig(
 *     @IniSection("ServerSettings")
 *     val serverConfig: ServerConfig,
 *
 *     @IniSection("GameplaySettings")
 *     val gameplayConfig: GameplayConfig
 * )
 * ```
 *
 * When the GameConfig class is serialized, it will produce:
 * ```ini
 * [ServerSettings]
 * serverName=MyServer
 * maxPlayers=50
 * enablePvP=True
 *
 * [GameplaySettings]
 * difficulty=0.5
 * isHardcore=False
 * ```
 *
 * Section name resolution:
 * 1. If [sectionName] is provided in the annotation, that name is used
 * 2. Otherwise, if the class itself has an @IniSection annotation with a name, that name is used
 * 3. Finally, if no explicit name is found, the simple class name is used
 *
 * @property sectionName The name of the section in the INI file. If not provided, the simple name of the class will be used.
 *
 * @see annotations.IniSerializable
 * @see annotations.IniProperty
 * @see annotations.IniBoolean
 * @see annotations.IniArray
 * @see annotations.IniStruct
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniSection(
    /**
     * The name of the section in the INI file.
     * If not provided, the simple name of the class will be used.
     */
    val sectionName: String = ""
)
