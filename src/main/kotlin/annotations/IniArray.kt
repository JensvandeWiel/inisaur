package annotations

import ArrayType

/**
 * Configures how a collection/array property should be serialized in INI format.
 *
 * This annotation allows customization of array representation in INI files,
 * supporting both comma-separated values and repeated key formats.
 *
 * Example:
 * ```kotlin
 * @IniSerializable
 * data class ModConfiguration(
 *     @IniArray(arrayType = ArrayType.CommaSeparatedArray)
 *     val enabledMods: List<String>,
 *
 *     @IniArray(arrayType = ArrayType.RepeatedLineArray)
 *     val serverCommands: List<String>
 * )
 * ```
 *
 * Will produce:
 * ```ini
 * [ModConfiguration]
 * enabledMods=mod1,mod2,mod3
 * serverCommands=command1
 * serverCommands=command2
 * serverCommands=command3
 * ```
 *
 * @property arrayType The type of array representation to use in the INI file.
 * @see ArrayType
 * @see annotations.IniSerializable
 * @see annotations.IniProperty
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniArray(
    /**
     * The type of array representation to use in the INI file.
     * Default is [ArrayType.CommaSeparatedArray].
     */
    val arrayType: ArrayType = ArrayType.CommaSeparatedArray
)
