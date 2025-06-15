package annotations

import IniFile

/**
 * Interface for classes that want to capture and manage ignored or unknown keys during deserialization.
 *
 * When a class implementing this interface is deserialized by IniSerializer, any keys from the INI file
 * that do not map to class properties will be collected in the [ignoredKeys] IniFile object instead of
 * being discarded.
 *
 * This is useful for:
 * - Preserving all data from an INI file, even if your data model doesn't explicitly model it
 * - Handling INI files with dynamic or user-defined sections and keys
 * - Round-trip processing where you want to read, modify, and write back an INI file without
 *   losing any original content
 *
 * Example:
 * ```kotlin
 * @IniSerializable
 * data class GameConfig(
 *     val serverSettings: ServerSettings,
 *     override val ignoredKeys: IniFile = IniFile() // Will hold any unrecognized keys
 * ) : WithIgnored
 * ```
 */
interface WithIgnored {
    /**
     * An [IniFile] that stores any keys from the source INI file that were not mapped to
     * properties in this class during deserialization.
     */
    val ignoredKeys: IniFile
}
