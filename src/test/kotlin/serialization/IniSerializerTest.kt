package serialization

import annotations.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import kotlin.reflect.KClass

class IniSerializerTest {

    @IniStruct
    data class Location(val x: Float, val y: Float, val z: Float, val otherLocation: Location? = null)

    @IniSerializable
    data class SimpleSettings(
        val stringValue: String,
        val intValue: Int,
        val floatValue: Float,
        val booleanValue: Boolean,
        val nullableString: String? = null
    )

    @IniSerializable(sectionName = "CustomSection")
    data class CustomSectionSettings(
        val value: String
    )

    @IniSerializable
    data class WithPropertyOverrides(
        @IniProperty(name = "CustomName")
        val originalName: String,

        @IniProperty(ignore = true)
        val ignoredProperty: String?,

        val normalProperty: String
    )

    @IniSerializable
    data class WithBooleanSettings(
        @IniBoolean(capitalized = true)
        val capitalizedBool: Boolean,

        @IniBoolean(capitalized = false)
        val lowercaseBool: Boolean
    )

    @IniSerializable
    data class WithArraySettings(
        @IniArray(arrayType = ArrayType.CommaSeparatedArray)
        val commaSeparatedArray: List<String?>,

        @IniArray(arrayType = ArrayType.RepeatedLineArray)
        val repeatedLineArray: List<String?>,

        // No annotation - should use CommaSeparatedArray by default
        val defaultArray: List<Int>,
    )

    @IniSerializable
    data class WithMapSettings(
        val indexedMap: Map<Int, String>,
        val namedMap: Map<String, String?>,
        val nullableValueMap: Map<String, String?>
    )

    @IniSerializable
    data class WithStructSettings(
        val location: Location,
        val nullableLocation: Location?
    )

    @IniSerializable
    data class ComplexSettings(
        val stringValue: String,
        val intValue: Int,
        val floatValue: Float,

        @IniBoolean(capitalized = true)
        val booleanValue: Boolean,

        val nullableString: String? = null,

        @IniArray(arrayType = ArrayType.CommaSeparatedArray)
        val stringArray: List<String?>,

        @IniArray(arrayType = ArrayType.RepeatedLineArray)
        val intArray: List<Int>,

        val defaultArray: List<String?>,

        val indexedMap: Map<Int, String>,
        val namedMap: Map<String, String?>,

        val location: Location,
        val nullableLocation: Location? = null
    )

    @Nested
    inner class BasicSerializationTests {
        @Test
        fun `test simple serialization and deserialization`() {
            val original = SimpleSettings(
                stringValue = "Test",
                intValue = 42,
                floatValue = 3.14f,
                booleanValue = true
            )

            val iniString = IniSerializer.serialize(original)
            val deserialized = IniSerializer.deserialize<SimpleSettings>(iniString)

            assertEquals(original, deserialized)
        }

        @Test
        fun `test null values`() {
            val original = SimpleSettings(
                stringValue = "Test",
                intValue = 42,
                floatValue = 3.14f,
                booleanValue = true,
                nullableString = null
            )

            val iniString = IniSerializer.serialize(original)
            val deserialized = IniSerializer.deserialize<SimpleSettings>(iniString)

            assertEquals(original, deserialized)
            assertNull(deserialized.nullableString)
        }

        @Test
        fun `test custom section name`() {
            val original = CustomSectionSettings("Value")

            val iniString = IniSerializer.serialize(original)
            assertTrue(iniString.contains("[CustomSection]"))

            val deserialized = IniSerializer.deserialize<CustomSectionSettings>(iniString)
            assertEquals(original, deserialized)
        }
    }

    @Nested
    inner class PropertyAnnotationTests {
        @Test
        fun `test property name override`() {
            val original = WithPropertyOverrides(
                originalName = "Original",
                ignoredProperty = "Ignored",
                normalProperty = "Normal"
            )

            val iniString = IniSerializer.serialize(original)

            // Check that the overridden name is used
            assertTrue(iniString.contains("CustomName=Original"))

            // Check that the ignored property is not included
            assertFalse(iniString.contains("ignoredProperty"))
            assertFalse(iniString.contains("Ignored"))

            // Check that normal properties are as expected
            assertTrue(iniString.contains("normalProperty=Normal"))

            val deserialized = IniSerializer.deserialize<WithPropertyOverrides>(iniString)

            assertEquals(original.originalName, deserialized.originalName)
            assertEquals(original.normalProperty, deserialized.normalProperty)

            // The ignored property should have its default value after deserialization
            assertEquals(null, deserialized.ignoredProperty)
        }

        @Test
        fun `test boolean capitalization`() {
            val original = WithBooleanSettings(
                capitalizedBool = true,
                lowercaseBool = false
            )

            val iniString = IniSerializer.serialize(original)

            assertTrue(iniString.contains("capitalizedBool=True"))
            assertTrue(iniString.contains("lowercaseBool=false"))

            val deserialized = IniSerializer.deserialize<WithBooleanSettings>(iniString)

            assertEquals(original, deserialized)
        }
    }

    @Nested
    inner class ArrayTests {
        @Test
        fun `test array serialization with different formats`() {
            val original = WithArraySettings(
                commaSeparatedArray = listOf("a", "b", "c"),
                repeatedLineArray = listOf("x", "y", "z"),
                defaultArray = listOf(1, 2, 3),
            )

            val iniString = IniSerializer.serialize(original)

            // Check comma-separated format
            assertTrue(iniString.contains("commaSeparatedArray=a,b,c"))

            // Check repeated line format (each value on separate line)
            assertTrue(iniString.contains("repeatedLineArray=x"))
            assertTrue(iniString.contains("repeatedLineArray=y"))
            assertTrue(iniString.contains("repeatedLineArray=z"))

            // Check default array format (comma-separated)
            assertTrue(iniString.contains("defaultArray=1,2,3"))

            val deserialized = IniSerializer.deserialize<WithArraySettings>(iniString)

            assertEquals(original.commaSeparatedArray, deserialized.commaSeparatedArray)
            assertEquals(original.repeatedLineArray, deserialized.repeatedLineArray)
            assertEquals(original.defaultArray, deserialized.defaultArray)
        }

        @Test
        fun `test array with trailing null`() {
            val original = WithArraySettings(
                commaSeparatedArray = listOf("a", "b", "c", null),
                repeatedLineArray = listOf("x", "y", "z", null),
                defaultArray = listOf(1, 2, 3),
            )

            val iniString = IniSerializer.serialize(original)
            val deserialized = IniSerializer.deserialize<WithArraySettings>(iniString)

            assertEquals(3, deserialized.commaSeparatedArray.size)
            assertEquals("a", deserialized.commaSeparatedArray[0])
            assertEquals("b", deserialized.commaSeparatedArray[1])
            assertEquals("c", deserialized.commaSeparatedArray[2])

            assertEquals(4, deserialized.repeatedLineArray.size)
            assertEquals("x", deserialized.repeatedLineArray[0])
            assertEquals("y", deserialized.repeatedLineArray[1])
            assertEquals("z", deserialized.repeatedLineArray[2])
            assertEquals(null, deserialized.repeatedLineArray[3])
            assertNull(deserialized.repeatedLineArray[3])
        }
    }

    @Nested
    inner class MapTests {
        @Test
        fun `test map serialization and deserialization`() {
            val original = WithMapSettings(
                indexedMap = mapOf(1 to "first", 2 to "second", 3 to "third"),
                namedMap = mapOf("key1" to "value1", "key2" to "value2"),
                nullableValueMap = mapOf("key1" to null, "key2" to "value2")
            )

            val iniString = IniSerializer.serialize(original)

            // Check indexed map format
            assertTrue(iniString.contains("indexedMap[1]=first"))
            assertTrue(iniString.contains("indexedMap[2]=second"))
            assertTrue(iniString.contains("indexedMap[3]=third"))

            // Check named map format
            assertTrue(iniString.contains("namedMap[key1]=value1"))
            assertTrue(iniString.contains("namedMap[key2]=value2"))

            // Check map with null values
            assertTrue(iniString.contains("nullableValueMap[key1]="))
            assertTrue(iniString.contains("nullableValueMap[key2]=value2"))

            val deserialized = IniSerializer.deserialize<WithMapSettings>(iniString)

            assertEquals(original.indexedMap, deserialized.indexedMap)
            assertEquals(original.namedMap, deserialized.namedMap)

            // Check that null values are preserved in maps
            assertEquals(2, deserialized.nullableValueMap.size)
            assertTrue(deserialized.nullableValueMap.containsKey("key1"))
            assertNull(deserialized.nullableValueMap["key1"])
            assertEquals("value2", deserialized.nullableValueMap["key2"])
        }
    }

    @Nested
    inner class StructTests {
        @Test
        fun `test struct serialization and deserialization`() {
            val nestedLocation = Location(50.0f, 75.0f, 125.0f, null)
            val location = Location(100.0f, 200.0f, 300.0f, nestedLocation)

            val original = WithStructSettings(
                location = location,
                nullableLocation = null
            )

            val iniString = IniSerializer.serialize(original)

            // Check struct format with nested structs
            assertTrue(iniString.contains("location=("))
            assertTrue(iniString.contains("x=100.0"))
            assertTrue(iniString.contains("y=200.0"))
            assertTrue(iniString.contains("z=300.0"))
            assertTrue(iniString.contains("otherLocation=("))
            assertTrue(iniString.contains("x=50.0"))
            assertTrue(iniString.contains("y=75.0"))
            assertTrue(iniString.contains("z=125.0"))

            // Check null struct
            assertTrue(iniString.contains("nullableLocation="))

            val deserialized = IniSerializer.deserialize<WithStructSettings>(iniString)

            assertEquals(original.location.x, deserialized.location.x)
            assertEquals(original.location.y, deserialized.location.y)
            assertEquals(original.location.z, deserialized.location.z)
            assertNotNull(deserialized.location.otherLocation)
            assertEquals(original.location.otherLocation?.x, deserialized.location.otherLocation?.x)
            assertEquals(original.location.otherLocation?.y, deserialized.location.otherLocation?.y)
            assertEquals(original.location.otherLocation?.z, deserialized.location.otherLocation?.z)
            assertNull(deserialized.location.otherLocation?.otherLocation)

            assertNull(deserialized.nullableLocation)
        }

        @Test
        fun `test nested null structure handling`() {
            // Create a location with a null nested location
            val location = Location(100.0f, 200.0f, 300.0f, null)
            val original = WithStructSettings(location, null)

            val iniString = IniSerializer.serialize(original)
            val deserialized = IniSerializer.deserialize<WithStructSettings>(iniString)

            assertEquals(original.location.x, deserialized.location.x)
            assertEquals(original.location.y, deserialized.location.y)
            assertEquals(original.location.z, deserialized.location.z)
            assertNull(deserialized.location.otherLocation)
            assertNull(deserialized.nullableLocation)
        }

        @Test
        fun `test empty struct creation for non-nullable fields`() {
            // Create an INI string with no fields for nested structure
            val iniString = """
            [WithStructSettings]
            location=(x=100.0,y=200.0,z=300.0)
            nullableLocation=
            """.trimIndent()

            val deserialized = IniSerializer.deserialize<WithStructSettings>(iniString)

            // The location should be created with the values
            assertEquals(100.0f, deserialized.location.x)
            assertEquals(200.0f, deserialized.location.y)
            assertEquals(300.0f, deserialized.location.z)

            // The nullable location should be null
            assertNull(deserialized.nullableLocation)
        }
    }

    @Nested
    inner class ComplexTests {
        @Test
        fun `test complex serialization and deserialization`() {
            val nestedLocation = Location(50.0f, 75.0f, 125.0f, null)
            val location = Location(100.0f, 200.0f, 300.0f, nestedLocation)

            val original = ComplexSettings(
                stringValue = "Test",
                intValue = 42,
                floatValue = 3.14f,
                booleanValue = true,
                nullableString = null,
                stringArray = listOf("a", "b", "c"),
                intArray = listOf(1, 2, 3),
                defaultArray = listOf("first", "third"),
                indexedMap = mapOf(1 to "first", 2 to "second"),
                namedMap = mapOf("key1" to null, "key2" to "value2"),
                location = location,
                nullableLocation = null
            )

            val iniString = IniSerializer.serialize(original)
            val deserialized = IniSerializer.deserialize<ComplexSettings>(iniString)

            // Verify all properties
            assertEquals(original.stringValue, deserialized.stringValue)
            assertEquals(original.intValue, deserialized.intValue)
            assertEquals(original.floatValue, deserialized.floatValue)
            assertEquals(original.booleanValue, deserialized.booleanValue)
            assertNull(deserialized.nullableString)

            // Arrays
            assertEquals(3, deserialized.stringArray.size)
            assertEquals("a", deserialized.stringArray[0])
            assertEquals("b", deserialized.stringArray[1])
            assertEquals("c", deserialized.stringArray[2])

            assertEquals(3, deserialized.intArray.size)
            assertEquals(1, deserialized.intArray[0])
            assertEquals(2, deserialized.intArray[1])
            assertEquals(3, deserialized.intArray[2])

            assertEquals(2, deserialized.defaultArray.size)
            assertEquals("first", deserialized.defaultArray[0])
            assertEquals("third", deserialized.defaultArray[1])

            // Maps
            assertEquals(2, deserialized.indexedMap.size)
            assertEquals("first", deserialized.indexedMap[1])
            assertEquals("second", deserialized.indexedMap[2])

            assertEquals(2, deserialized.namedMap.size)
            assertNull(deserialized.namedMap["key1"])
            assertEquals("value2", deserialized.namedMap["key2"])

            // Structs
            assertEquals(original.location.x, deserialized.location.x)
            assertEquals(original.location.y, deserialized.location.y)
            assertEquals(original.location.z, deserialized.location.z)
            assertNotNull(deserialized.location.otherLocation)
            assertEquals(original.location.otherLocation?.x, deserialized.location.otherLocation?.x)
            assertEquals(original.location.otherLocation?.y, deserialized.location.otherLocation?.y)
            assertEquals(original.location.otherLocation?.z, deserialized.location.otherLocation?.z)

            assertNull(deserialized.nullableLocation)
        }
    }

    @Nested
    inner class ErrorHandlingTests {
        @Test
        fun `test missing section`() {
            val iniString = """
            [WrongSection]
            value=test
            """.trimIndent()

            assertThrows(IllegalArgumentException::class.java) {
                IniSerializer.deserialize<SimpleSettings>(iniString)
            }
        }

        @Test
        fun `test serialization of non-annotated class`() {
            class NotAnnotated(val value: String)

            assertThrows(IllegalArgumentException::class.java) {
                IniSerializer.serialize(NotAnnotated("test"))
            }
        }

        @Test
        fun `test deserialization to non-annotated class`() {
            class NotAnnotated(val value: String)

            val iniString = """
            [NotAnnotated]
            value=test
            """.trimIndent()

            assertThrows(IllegalArgumentException::class.java) {
                IniSerializer.deserialize(iniString, NotAnnotated::class)
            }
        }
    }
}
