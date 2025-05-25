package serialization

import IniFile
import Lexer
import Parser
import Section
import annotations.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * The serialization engine for converting between Kotlin objects and INI file format.
 *
 * IniSerializer provides functionality to:
 * - Convert Kotlin objects to INI file content using annotations
 * - Parse INI file content into Kotlin objects
 * - Handle various data types including primitives, collections, maps, and nested objects
 *
 * The serialization process is driven by annotations:
 * - [IniSerializable] - Marks a class as serializable to/from INI format
 * - [IniProperty] - Configures how a property should be serialized
 * - [IniBoolean] - Configures boolean formatting (capitalized or lowercase)
 * - [IniArray] - Configures how collections should be formatted
 * - [IniStruct] - Marks a nested class as a struct value
 *
 * Example usage:
 * ```kotlin
 * // Define an annotated class
 * @IniSerializable("ServerSettings")
 * data class ServerConfig(
 *     val serverName: String,
 *
 *     @IniBoolean(capitalized = true)
 *     val enablePvP: Boolean,
 *
 *     @IniArray(arrayType = ArrayType.CommaSeparatedArray)
 *     val enabledMods: List<String>
 * )
 *
 * // Serialize an object to INI string
 * val config = ServerConfig("My Server", true, listOf("mod1", "mod2"))
 * val iniContent = IniSerializer.serialize(config)
 *
 * // Deserialize INI string back to an object
 * val parsedConfig = IniSerializer.deserialize<ServerConfig>(iniContent)
 * ```
 *
 * @see IniSerializable
 * @see IniProperty
 * @see IniBoolean
 * @see IniArray
 * @see IniStruct
 */
object IniSerializer {
    /**
     * Serializes an object to an INI file format string.
     *
     * The object's class must be annotated with [IniSerializable]. Each property of the class
     * is processed according to its type and any annotations applied to it.
     *
     * @param obj The object to serialize
     * @return The INI file content as a string
     * @throws IllegalArgumentException if the object's class is not annotated with [IniSerializable]
     *
     * @see IniSerializable
     */
    @Throws(IllegalArgumentException::class)
    fun <T : Any> serialize(obj: T): String {
        val iniFile = objectToIniFile(obj)
        return iniFile.toString()
    }

    /**
     * Serializes an object to an IniFile instance.
     *
     * Similar to [serialize], but returns an [IniFile] instance instead of a string,
     * allowing for further manipulation of the INI data.
     *
     * @param obj The object to serialize
     * @return An [IniFile] representation of the object
     * @throws IllegalArgumentException if the object's class is not annotated with [IniSerializable]
     *
     * @see IniSerializable
     * @see IniFile
     */
    @Throws(IllegalArgumentException::class)
    fun <T : Any> serializeToIniFile(obj: T): IniFile {
        return objectToIniFile(obj)
    }

    /**
     * Deserializes an INI string to an object of the specified class.
     *
     * The class must be annotated with [IniSerializable]. The method will attempt to
     * match properties in the INI file with constructor parameters of the target class.
     *
     * @param iniContent The INI content string
     * @param clazz The Kotlin class to deserialize to
     * @return An instance of the specified class
     * @throws IllegalArgumentException if the class is not annotated with [IniSerializable]
     * or if a required section is not found in the INI content
     *
     * @see IniSerializable
     */
    @Throws(IllegalArgumentException::class)
    fun <T : Any> deserialize(iniContent: String, clazz: KClass<T>): T {
        if (!clazz.hasAnnotation<IniSerializable>()) {
            throw IllegalArgumentException("Class ${clazz.simpleName} must be annotated with @IniSerializable")
        }

        val lexer = Lexer(iniContent)
        val parser = Parser(lexer)
        val iniFile = parser.parse()

        return iniFileToObject(iniFile, clazz)
    }

    /**
     * Deserializes an INI file instance to an object of the specified class.
     *
     * Similar to [deserialize], but takes an [IniFile] instance instead of a string.
     *
     * @param iniFile The [IniFile] to deserialize
     * @param clazz The Kotlin class to deserialize to
     * @return An instance of the specified class
     * @throws IllegalArgumentException if the class is not annotated with [IniSerializable]
     * or if a required section is not found in the INI file
     *
     * @see IniSerializable
     * @see IniFile
     */
    @Throws(IllegalArgumentException::class)
    fun <T : Any> deserialize(iniFile: IniFile, clazz: KClass<T>): T {
        return iniFileToObject(iniFile, clazz)
    }

    /**
     * Convenience method to deserialize an INI string to an object using type inference.
     *
     * This inline function allows for a more concise syntax when the target type can be
     * inferred from the context.
     *
     * Example:
     * ```kotlin
     * val config = IniSerializer.deserialize<ServerConfig>(iniContent)
     * ```
     *
     * @param iniContent The INI content string
     * @return An instance of the inferred type
     * @throws IllegalArgumentException if the inferred class is not annotated with [IniSerializable]
     *
     * @see IniSerializable
     */
    inline fun <reified T : Any> deserialize(iniContent: String): T {
        return deserialize(iniContent, T::class)
    }

    /**
     * Convenience method to deserialize an INI file to an object using type inference.
     *
     * Similar to the string version of [deserialize], but takes an [IniFile] instance.
     *
     * @param iniFile The [IniFile] to deserialize
     * @return An instance of the inferred type
     * @throws IllegalArgumentException if the inferred class is not annotated with [IniSerializable]
     *
     * @see IniSerializable
     * @see IniFile
     */
    inline fun <reified T : Any> deserialize(iniFile: IniFile): T {
        return deserialize(iniFile, T::class)
    }

    /**
     * Converts an object to an IniFile.
     *
     * @param obj The object to convert
     * @return An IniFile representation of the object
     * @throws IllegalArgumentException if the object class is not annotated with [IniSerializable]
     */
    @Throws(IllegalArgumentException::class)
    private fun <T : Any> objectToIniFile(obj: T): IniFile {
        val clazz = obj::class

        if (!clazz.hasAnnotation<IniSerializable>()) {
            throw IllegalArgumentException("Class ${clazz.simpleName} must be annotated with @IniSerializable")
        }

        val sectionAnnotation = clazz.findAnnotation<IniSerializable>()
        val sectionName = if (sectionAnnotation?.sectionName?.isNotEmpty() == true) {
            sectionAnnotation.sectionName
        } else {
            clazz.simpleName ?: "UnnamedSection"
        }

        val sections = mutableListOf<Section>()
        val section = Section(sectionName)

        // Process all properties
        for (property in clazz.memberProperties) {
            val propertyAnnotation = property.findAnnotation<IniProperty>()

            // Skip ignored properties
            if (propertyAnnotation?.ignore == true) {
                continue
            }

            val propertyName = propertyAnnotation?.name?.takeIf { it.isNotEmpty() } ?: property.name
            val value = property.getter.call(obj)

            processProperty(property, propertyName, value, section)
        }

        sections.add(section)
        return IniFile(sections)
    }

    /**
     * Processes a property and adds it to the section.
     *
     * This method handles different property types and applies appropriate annotations.
     *
     * @param property The Kotlin property to process
     * @param propertyName The name to use for the property in the INI file
     * @param value The value of the property
     * @param section The section to add the property to
     */
    private fun processProperty(property: KProperty1<*, *>, propertyName: String, value: Any?, section: Section) {
        val arrayAnnotation = property.findAnnotation<IniArray>()

        when {
            // Handle arrays/collections - with or without annotation
            value is Collection<*> -> {
                // If there's no annotation, use CommaSeparatedArray by default
                val arrayType = arrayAnnotation?.arrayType ?: ArrayType.CommaSeparatedArray

                when (arrayType) {
                    ArrayType.CommaSeparatedArray -> {
                        val valuesList = value.map { convertToIniValue(it) }
                        section.addArrayKey(propertyName, valuesList, ArrayType.CommaSeparatedArray)
                    }
                    ArrayType.RepeatedLineArray -> {
                        val valuesList = value.map { convertToIniValue(it) }
                        section.addArrayKey(propertyName, valuesList, ArrayType.RepeatedLineArray)
                    }
                }
            }

            // Handle maps that need to be indexed arrays
            value is Map<*, *> && value.keys.all { it is Int } -> {
                val indexedMap = value.entries.associate { (k, v) ->
                    (k as Int) to convertToIniValue(v)
                }
                section.addIndexedArrayKey(propertyName, indexedMap)
            }

            // Handle maps that need to be named maps
            value is Map<*, *> && value.keys.all { it is String } -> {
                val namedMap = value.entries.associate { (k, v) ->
                    (k as String) to convertToIniValue(v)
                }
                section.addMapKey(propertyName, namedMap)
            }

            // Handle structs (objects with @IniStruct annotation)
            value != null && value::class.hasAnnotation<IniStruct>() -> {
                val structMap = convertObjectToStructMap(value)
                section.addKey(propertyName, structMap)
            }

            // Handle boolean with IniBoolean annotation
            value is Boolean? && property.findAnnotation<IniBoolean>() != null -> {
                val boolAnnotation = property.findAnnotation<IniBoolean>()!!
                section.addKey(propertyName, value, boolAnnotation.capitalized)
            }

            // Handle basic types
            else -> {
                when (value) {
                    is String -> section.addKey(propertyName, value)
                    is Int -> section.addKey(propertyName, value)
                    is Float -> section.addKey(propertyName, value)
                    is Boolean -> section.addKey(propertyName, value)
                    null -> section.addKey(propertyName, null as String?)
                }
            }
        }
    }

    /**
     * Converts an object to a map for struct serialization.
     *
     * This method transforms an object annotated with [IniStruct] into a map of
     * property names to values, which can be serialized as a struct in INI format.
     *
     * @param obj The object to convert
     * @return A map representation of the object
     */
    private fun convertObjectToStructMap(obj: Any): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        for (property in obj::class.memberProperties) {
            val propertyAnnotation = property.findAnnotation<IniProperty>()

            // Skip ignored properties
            if (propertyAnnotation?.ignore == true) {
                continue
            }

            val propertyName = propertyAnnotation?.name?.takeIf { it.isNotEmpty() } ?: property.name
            val value = property.getter.call(obj)

            // For nested structs
            val propValue = if (value != null && value::class.hasAnnotation<IniStruct>()) {
                convertObjectToStructMap(value)
            } else {
                value
            }

            result[propertyName] = propValue
        }

        return result
    }

    /**
     * Helper method to convert a value to an appropriate INI value.
     *
     * @param value The value to convert
     * @return The converted value suitable for INI representation
     */
    private fun convertToIniValue(value: Any?): Any? {
        return when (value) {
            null -> null
            is String, is Int, is Float, is Boolean -> value
            value::class.hasAnnotation<IniStruct>() -> convertObjectToStructMap(value)
            else -> value.toString()
        }
    }

    /**
     * Converts an IniFile to an object of the specified class.
     *
     * This method handles the deserialization of INI data into a Kotlin object,
     * matching section entries to class properties.
     *
     * @param iniFile The IniFile to convert
     * @param clazz The class to convert to
     * @return An instance of the specified class
     * @throws IllegalArgumentException if the class is not annotated with [IniSerializable]
     * or if a required section is not found
     */
    @Throws(IllegalArgumentException::class)
    private fun <T : Any> iniFileToObject(iniFile: IniFile, clazz: KClass<T>): T {
        if (!clazz.hasAnnotation<IniSerializable>()) {
            throw IllegalArgumentException("Class ${clazz.simpleName} must be annotated with @IniSerializable")
        }

        val sectionAnnotation = clazz.findAnnotation<IniSerializable>()
        val sectionName = if (sectionAnnotation?.sectionName?.isNotEmpty() == true) {
            sectionAnnotation.sectionName
        } else {
            clazz.simpleName ?: "UnnamedSection"
        }

        // Check if section exists
        if (!iniFile.hasSection(sectionName)) {
            throw IllegalArgumentException("Section $sectionName not found in INI file")
        }

        val section = iniFile.getSection(sectionName)

        // Prepare constructor parameters
        val constructor = clazz.primaryConstructor
            ?: throw IllegalArgumentException("Class ${clazz.simpleName} must have a primary constructor")

        val parameters = constructor.parameters
        val parameterValues = mutableMapOf<String, Any?>()

        // Process all properties that match constructor parameters
        for (property in clazz.memberProperties) {
            val propertyAnnotation = property.findAnnotation<IniProperty>()

            // Skip ignored properties
            if (propertyAnnotation?.ignore == true) {
                continue
            }

            val propertyName = propertyAnnotation?.name?.takeIf { it.isNotEmpty() } ?: property.name
            val paramName = property.name

            // Find matching constructor parameter
            val parameter = parameters.find { it.name == paramName }
                ?: continue

            try {
                val value = extractValueFromSection(section, propertyName, property)
                parameterValues[paramName] = value
            } catch (e: Exception) {
                // If we can't get the value, leave it as default or null
                continue
            }
        }

        return constructor.callBy(parameters.associateWith { parameterValues[it.name] })
    }

    /**
     * Extracts a value from a section based on property annotations.
     *
     * This method handles the extraction of values from the INI section, taking into
     * account property types and annotations.
     *
     * @param section The section to extract values from
     * @param propertyName The name of the property in the INI file
     * @param property The Kotlin property to extract value for
     * @return The extracted value, converted to the appropriate type
     */
    private fun extractValueFromSection(section: Section, propertyName: String, property: KProperty1<*, *>): Any? {
        val returnType = property.returnType.classifier as? KClass<*> ?: return null
        val arrayAnnotation = property.findAnnotation<IniArray>()

        // Check if this is a required non-null property
        val isNullable = property.returnType.isMarkedNullable

        return when {
            // Handle arrays/collections
            Collection::class.java.isAssignableFrom(returnType.java) -> {
                // If there's no annotation, use CommaSeparatedArray by default (same as in serialization)
                val arrayType = arrayAnnotation?.arrayType ?: ArrayType.CommaSeparatedArray

                try {
                    val arrayValues = when (arrayType) {
                        ArrayType.CommaSeparatedArray -> section.getArrayKey(propertyName)
                        ArrayType.RepeatedLineArray -> section.getArrayKey(propertyName)
                    }
                    processArrayValues(arrayValues)
                } catch (e: Exception) {
                    emptyList<Any?>()
                }
            }

            // Handle boolean with IniBoolean annotation
            returnType == Boolean::class -> {
                try {
                    section.getBooleanKey(propertyName)
                } catch (e: Exception) {
                    if (!isNullable) false else null
                }
            }

            // Handle maps for indexed arrays
            Map::class.java.isAssignableFrom(returnType.java) -> {
                try {
                    // Try as indexed array first
                    val indexedMap = section.getIndexedArrayKey(propertyName)
                    // Process the map to convert empty strings to nulls if the map can contain nullable values
                    processMapValues(indexedMap)
                } catch (e: Exception) {
                    try {
                        // Then try as map entry
                        val namedMap = section.getMapKey(propertyName)
                        // Process the map to convert empty strings to nulls if the map can contain nullable values
                        processMapValues(namedMap)
                    } catch (e2: Exception) {
                        emptyMap<String, Any?>()
                    }
                }
            }

            // Handle basic types
            returnType == String::class -> {
                try {
                    val value = section.getStringKey(propertyName)
                    // For nullable String properties, empty string should be converted to null
                    if (isNullable && value != null && value.isEmpty()) {
                        null
                    } else {
                        value
                    }
                } catch (e: Exception) {
                    if (!isNullable) "" else null
                }
            }

            returnType == Int::class -> {
                try {
                    section.getIntKey(propertyName)
                } catch (e: Exception) {
                    if (!isNullable) 0 else null
                }
            }

            returnType == Float::class -> {
                try {
                    section.getFloatKey(propertyName)
                } catch (e: Exception) {
                    if (!isNullable) 0.0f else null
                }
            }

            // Handle structs (classes with @IniStruct annotation)
            returnType.hasAnnotation<IniStruct>() -> {
                try {
                    val structMap = section.getStructKey(propertyName)
                    createStructInstance(returnType, structMap)
                } catch (e: Exception) {
                    // If this property is required and non-null, create an empty struct instance
                    if (!isNullable) {
                        createEmptyInstance(returnType)
                    } else {
                        null
                    }
                }
            }

            // Default
            else -> null
        }
    }

    /**
     * Process map values to convert empty strings to nulls for maps that can contain nullable values.
     *
     * This is always applied to maps with Any? or String? value types to ensure empty strings are converted to nulls.
     *
     * @param map The map to process
     * @param forceNullConversion Whether to force conversion of empty strings to nulls
     * @return The processed map
     */
    private fun <K> processMapValues(map: Map<K, Any?>, forceNullConversion: Boolean = true): Map<K, Any?> {
        return map.mapValues { (_, value) ->
            when {
                // Always convert empty strings to nulls
                value is String && value.isEmpty() -> null
                // Recursively process nested maps
                value is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    processMapValues(value as Map<String, Any?>, forceNullConversion)
                }
                else -> value
            }
        }
    }

    /**
     * Process array values to convert empty strings to nulls.
     *
     * @param list The list to process
     * @return The processed list
     */
    private fun processArrayValues(list: List<Any?>): List<Any?> {
        return list.map { value ->
            when {
                value is String && value.isEmpty() -> null
                value is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    processMapValues(value as Map<String, Any?>)
                }
                else -> value
            }
        }
    }

    /**
     * Creates an instance of a struct class from a map.
     *
     * This method constructs an object of a class annotated with [IniStruct]
     * using the provided property values.
     *
     * @param clazz The class to instantiate
     * @param structMap The map of property names to values
     * @return The created instance, or null if instantiation failed
     */
    private fun <T : Any> createStructInstance(clazz: KClass<T>, structMap: Map<String, Any?>): T? {
        val constructor = clazz.primaryConstructor ?: return null
        val parameters = constructor.parameters
        val parameterValues = mutableMapOf<String, Any?>()

        // Map struct properties to constructor parameters
        for (parameter in parameters) {
            val paramName = parameter.name ?: continue

            // Find property annotation to check for name override
            val property = clazz.memberProperties.find { it.name == paramName } ?: continue
            val propertyAnnotation = property.findAnnotation<IniProperty>()
            val keyName = propertyAnnotation?.name?.takeIf { it.isNotEmpty() } ?: paramName

            // Look up the value in the struct map
            val value = structMap[keyName]

            // Handle nested structs
            val parameterType = parameter.type.classifier as? KClass<*>
            if (parameterType != null && parameterType.hasAnnotation<IniStruct>()) {
                if (value is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val nestedValue = createStructInstance(parameterType, value as Map<String, Any?>)
                    parameterValues[paramName] = nestedValue
                } else if (!parameter.isOptional && !parameter.type.isMarkedNullable) {
                    // If the parameter is required and non-null, create an empty instance
                    parameterValues[paramName] = createEmptyInstance(parameterType)
                }
            } else {
                parameterValues[paramName] = value
            }
        }

        // Filter out null values for non-nullable parameters
        val finalParams = parameters.associateWith { param ->
            val value = parameterValues[param.name]
            if (value == null && !param.type.isMarkedNullable && !param.isOptional) {
                // For non-nullable parameters that have null values, create default instances if possible
                val paramType = param.type.classifier as? KClass<*>
                if (paramType != null && paramType.hasAnnotation<IniStruct>()) {
                    createEmptyInstance(paramType)
                } else {
                    // Use appropriate default values for primitive types
                    when(paramType) {
                        String::class -> ""
                        Int::class -> 0
                        Float::class -> 0.0f
                        Boolean::class -> false
                        else -> value
                    }
                }
            } else {
                value
            }
        }

        return try {
            constructor.callBy(finalParams)
        } catch (e: IllegalArgumentException) {
            // If we can't construct the object with the given parameters, return null
            null
        }
    }

    /**
     * Creates an empty instance of a class with default values.
     */
    private fun <T : Any> createEmptyInstance(clazz: KClass<T>): T? {
        val constructor = clazz.primaryConstructor ?: return null

        // Create a map of parameters with default values or null for optional parameters
        val paramMap = constructor.parameters.associateWith { param ->
            if (param.isOptional) {
                // Use default value if available
                null
            } else if (!param.type.isMarkedNullable) {
                // For non-nullable types, try to create appropriate default values
                val paramType = param.type.classifier as? KClass<*>
                when {
                    paramType == String::class -> ""
                    paramType == Int::class -> 0
                    paramType == Float::class -> 0.0f
                    paramType == Boolean::class -> false
                    paramType?.hasAnnotation<IniStruct>() == true -> createEmptyInstance(paramType)
                    Collection::class.java.isAssignableFrom(paramType?.java ?: Any::class.java) -> emptyList<Any>()
                    Map::class.java.isAssignableFrom(paramType?.java ?: Any::class.java) -> emptyMap<Any, Any>()
                    else -> null
                }
            } else {
                null
            }
        }

        return try {
            constructor.callBy(paramMap)
        } catch (e: Exception) {
            null
        }
    }
}
