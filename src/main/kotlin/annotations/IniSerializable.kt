package annotations

/**
 * Marks a class as serializable to INI format.
 * Classes with this annotation can be converted to INI sections.
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
