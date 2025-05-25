package annotations

/**
 * Configures how a property should be serialized in INI format.
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
