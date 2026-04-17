package annotations

/**
 * Annotation to indicate that a property should be omitted from the INI file if its value is null.
 *
 * This annotation can be used on properties of classes marked with [IniSection] or [IniStruct].
 *
 * Example:
 * ```kotlin
 * @IniSection
 * data class Config(
 *     @OmitIfNull
 *     val optionalValue: String? = null
 * )
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class OmitIfNull
