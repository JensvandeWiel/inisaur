package annotations

/**
 * Marks a class as a full INI file serializable to/from INI format.
 *
 * Classes with this annotation represent a complete INI file with multiple sections.
 * Each property in the class that is annotated with [IniSection] will be serialized
 * as a separate section in the INI file.
 *
 * Example:
 * ```kotlin
 * @IniSerializable
 * data class GameConfig(
 *     @IniSection("ServerSettings")
 *     val serverConfig: ServerConfig,
 *
 *     @IniSection("GameplaySettings")
 *     val gameplayConfig: GameplayConfig
 * )
 *
 * // Section classes
 * @IniSection
 * data class ServerConfig(
 *     val serverName: String,
 *     val maxPlayers: Int
 * )
 *
 * @IniSection
 * data class GameplayConfig(
 *     val difficulty: Float,
 *     val enablePvP: Boolean
 * )
 * ```
 *
 * Will produce:
 * ```ini
 * [ServerSettings]
 * serverName=My Server
 * maxPlayers=50
 *
 * [GameplaySettings]
 * difficulty=0.5
 * enablePvP=True
 * ```
 *
 * To serialize and deserialize full INI files:
 *
 * ```kotlin
 * // Serialization
 * val gameConfig = GameConfig(
 *     serverConfig = ServerConfig("My Server", 50),
 *     gameplayConfig = GameplayConfig(0.5f, true)
 * )
 * val iniString = IniSerializer.serialize(gameConfig)
 *
 * // Deserialization
 * val parsedConfig = IniSerializer.deserialize<GameConfig>(iniString)
 * ```
 *
 * The serializer will:
 * 1. Process each property marked with [IniSection]
 * 2. Find the appropriate section name (from annotation or class name)
 * 3. Generate section content from the property's value
 * 4. Combine all sections into a complete INI file
 *
 * @see annotations.IniSection
 * @see annotations.IniProperty
 * @see annotations.IniBoolean
 * @see annotations.IniArray
 * @see annotations.IniStruct
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniSerializable
