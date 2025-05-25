package annotations

import ArrayType

/**
 * Configures how a collection/array property should be serialized in INI format.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniArray(
    /**
     * The type of array representation to use in the INI file.
     */
    val arrayType: ArrayType = ArrayType.CommaSeparatedArray
)
