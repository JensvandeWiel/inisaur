package annotations

/**
 * Configures how a Boolean property should be serialized in INI format.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniBoolean(
    /**
     * Whether the boolean value should be capitalized (True/False) or lowercase (true/false).
     */
    val capitalized: Boolean = true
)
