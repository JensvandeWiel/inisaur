package annotations

/**
 * Marks a class as an INI struct value.
 * Classes with this annotation can be serialized as struct values within INI files.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IniStruct
