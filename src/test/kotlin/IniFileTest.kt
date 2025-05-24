import exceptions.InvalidTypeException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IniFileTest {

    @Test
    fun `test section management functions`() {
        val iniFile = IniFile(emptyList())

        // Check that initially there are no sections
        assertFalse(iniFile.hasSection("test"))

        // Add a section and verify it exists
        val section = iniFile.addSection("test")
        assertEquals("test", section.name)
        assertTrue(iniFile.hasSection("test"))

        // Get the section and verify it's the same
        val retrievedSection = iniFile.getSection("test")
        assertEquals(section, retrievedSection)

        // Delete the section and verify it no longer exists
        iniFile.deleteSection("test")
        assertFalse(iniFile.hasSection("test"))

        // Verify getting a non-existent section throws an exception
        val exception = assertThrows<NoSuchElementException> {
            iniFile.getSection("nonexistent")
        }
        assertEquals("Section 'nonexistent' not found", exception.message)

        // Verify deleting a non-existent section throws an exception
        val deleteException = assertThrows<NoSuchElementException> {
            iniFile.deleteSection("nonexistent")
        }
        assertEquals("Section 'nonexistent' not found", deleteException.message)
    }

    @Test
    fun `test string value operations`() {
        val iniFile = IniFile(emptyList())

        // Add a string value to a new section
        iniFile.addValue("section1", "key1", "value1")

        // Verify the section and value exist
        assertTrue(iniFile.hasSection("section1"))
        assertEquals("value1", iniFile.getStringValue("section1", "key1"))

        // Set a string value
        iniFile.setValue("section1", "key1", "updated_value")
        assertEquals("updated_value", iniFile.getStringValue("section1", "key1"))

        // Add a string value to an existing section
        iniFile.addValue("section1", "key2", "value2")
        assertEquals("value2", iniFile.getStringValue("section1", "key2"))

        // Delete a value
        iniFile.deleteValue("section1", "key1")
        assertThrows<NoSuchElementException> {
            iniFile.getStringValue("section1", "key1")
        }

        // Test null string value
        iniFile.setValue("section1", "nullkey", null as String?)
        assertNull(iniFile.getStringValue("section1", "nullkey"))
    }

    @Test
    fun `test int value operations`() {
        val iniFile = IniFile(emptyList())

        // Add an int value to a new section
        iniFile.addValue("section1", "key1", 123)

        // Verify the section and value exist
        assertTrue(iniFile.hasSection("section1"))
        assertEquals(123, iniFile.getIntValue("section1", "key1"))

        // Set an int value
        iniFile.setValue("section1", "key1", 456)
        assertEquals(456, iniFile.getIntValue("section1", "key1"))

        // Add an int value to an existing section
        iniFile.addValue("section1", "key2", 789)
        assertEquals(789, iniFile.getIntValue("section1", "key2"))

        // Test null int value
        iniFile.setValue("section1", "nullkey", null as Int?)
        assertNull(iniFile.getIntValue("section1", "nullkey"))

        // Test type exception
        assertThrows<InvalidTypeException> {
            iniFile.getStringValue("section1", "key1") // This is an int, not a string
        }
    }

    @Test
    fun `test float value operations`() {
        val iniFile = IniFile(emptyList())

        // Add a float value to a new section
        iniFile.addValue("section1", "key1", 123.45f)

        // Verify the section and value exist
        assertTrue(iniFile.hasSection("section1"))
        assertEquals(123.45f, iniFile.getFloatValue("section1", "key1"))

        // Set a float value
        iniFile.setValue("section1", "key1", 456.78f)
        assertEquals(456.78f, iniFile.getFloatValue("section1", "key1"))

        // Add a float value to an existing section
        iniFile.addValue("section1", "key2", 789.01f)
        assertEquals(789.01f, iniFile.getFloatValue("section1", "key2"))

        // Test null float value
        iniFile.setValue("section1", "nullkey", null as Float?)
        assertNull(iniFile.getFloatValue("section1", "nullkey"))

        // Test type exception
        assertThrows<InvalidTypeException> {
            iniFile.getIntValue("section1", "key1") // This is a float, not an int
        }
    }

    @Test
    fun `test boolean value operations`() {
        val iniFile = IniFile(emptyList())

        // Add a boolean value to a new section
        iniFile.addValue("section1", "key1", true)

        // Verify the section and value exist
        assertTrue(iniFile.hasSection("section1"))
        assertEquals(true, iniFile.getBooleanValue("section1", "key1"))

        // Set a boolean value
        iniFile.setValue("section1", "key1", false)
        assertEquals(false, iniFile.getBooleanValue("section1", "key1"))

        // Add a boolean value with capitalization parameter
        iniFile.addValue("section1", "key2", true, false)
        assertEquals(true, iniFile.getBooleanValue("section1", "key2"))

        // Set a boolean value with capitalization parameter
        iniFile.setValue("section1", "key2", false, false)
        assertEquals(false, iniFile.getBooleanValue("section1", "key2"))

        // Test null boolean value
        iniFile.setValue("section1", "nullkey", null as Boolean?)
        assertNull(iniFile.getBooleanValue("section1", "nullkey"))

        // Test type exception
        assertThrows<InvalidTypeException> {
            iniFile.getStringValue("section1", "key1") // This is a boolean, not a string
        }
    }

    @Test
    fun `test struct value operations`() {
        val iniFile = IniFile(emptyList())

        // Create a struct
        val struct = mapOf(
            "field1" to "value1",
            "field2" to 123,
            "field3" to true
        )

        // Add a struct value to a new section
        iniFile.addValue("section1", "key1", struct)

        // Verify the section and value exist
        assertTrue(iniFile.hasSection("section1"))
        val retrievedStruct = iniFile.getStructValue("section1", "key1")
        assertEquals(3, retrievedStruct.size)
        assertEquals("value1", retrievedStruct["field1"])
        assertEquals(123, retrievedStruct["field2"])
        assertEquals(true, retrievedStruct["field3"])

        // Set a struct value
        val updatedStruct = mapOf(
            "field1" to "updated_value",
            "field2" to 456,
            "field3" to false
        )
        iniFile.setValue("section1", "key1", updatedStruct)
        val retrievedUpdatedStruct = iniFile.getStructValue("section1", "key1")
        assertEquals("updated_value", retrievedUpdatedStruct["field1"])
        assertEquals(456, retrievedUpdatedStruct["field2"])
        assertEquals(false, retrievedUpdatedStruct["field3"])

        // Test type exception
        assertThrows<InvalidTypeException> {
            iniFile.getStringValue("section1", "key1") // This is a struct, not a string
        }
    }

    @Test
    fun `test array value operations`() {
        val iniFile = IniFile(emptyList())

        // Create an array
        val array = listOf("value1", 123, true)

        // Add an array value to a new section
        iniFile.addArrayValue("section1", "key1", array)

        // Verify the section and value exist
        assertTrue(iniFile.hasSection("section1"))
        val retrievedArray = iniFile.getArrayValue("section1", "key1")
        assertEquals(3, retrievedArray.size)
        assertEquals("value1", retrievedArray[0])
        assertEquals(123, retrievedArray[1])
        assertEquals(true, retrievedArray[2])

        // Set an array value
        val updatedArray = listOf("updated_value", 456, false)
        iniFile.setArrayValue("section1", "key1", updatedArray)
        val retrievedUpdatedArray = iniFile.getArrayValue("section1", "key1")
        assertEquals("updated_value", retrievedUpdatedArray[0])
        assertEquals(456, retrievedUpdatedArray[1])
        assertEquals(false, retrievedUpdatedArray[2])

        // Test type exception
        assertThrows<InvalidTypeException> {
            iniFile.getStringValue("section1", "key1") // This is an array, not a string
        }

        // Test with ArrayType.RepeatedLineArray
        iniFile.addArrayValue("section1", "key2", array, ArrayType.RepeatedLineArray)
        val retrievedRepeatedArray = iniFile.getArrayValue("section1", "key2")
        assertEquals(3, retrievedRepeatedArray.size)
        assertEquals("value1", retrievedRepeatedArray[0])
        assertEquals(123, retrievedRepeatedArray[1])
        assertEquals(true, retrievedRepeatedArray[2])
    }

    @Test
    fun `test indexed array value operations`() {
        val iniFile = IniFile(emptyList())

        // Create an indexed array
        val indexedArray = mapOf(
            0 to "value1",
            1 to 123,
            2 to true
        )

        // Add an indexed array value to a new section
        iniFile.addIndexedArrayValue("section1", "key1", indexedArray)

        // Verify the section and value exist
        assertTrue(iniFile.hasSection("section1"))
        val retrievedArray = iniFile.getIndexedArrayValue("section1", "key1")
        assertEquals(3, retrievedArray.size)

        // Set an indexed array value
        val updatedIndexedArray = mapOf(
            0 to "updated_value",
            1 to 456,
            2 to false
        )
        iniFile.setIndexedArrayValue("section1", "key1", updatedIndexedArray)
        val retrievedUpdatedArray = iniFile.getIndexedArrayValue("section1", "key1")
        assertEquals(3, retrievedUpdatedArray.size)

        // Test type exception
        assertThrows<InvalidTypeException> {
            iniFile.getStringValue("section1", "key1") // This is an indexed array, not a string
        }
    }

    @Test
    fun `test map value operations`() {
        val iniFile = IniFile(emptyList())

        // Create a map
        val map = mapOf(
            "key1" to "value1",
            "key2" to 123,
            "key3" to true
        )

        // Add a map value to a new section
        iniFile.addMapValue("section1", "mapKey", map)

        // Set a map value
        val updatedMap = mapOf(
            "key1" to "updated_value",
            "key2" to 456,
            "key3" to false
        )
        iniFile.setMapValue("section1", "mapKey", updatedMap)

        // Test type exception
        assertThrows<InvalidTypeException> {
            iniFile.getStringValue("section1", "mapKey") // This is a map, not a string
        }
    }

    @Test
    fun `test duplicate key error`() {
        val iniFile = IniFile(emptyList())

        // Add a value
        iniFile.addValue("section1", "key1", "value1")

        // Try to add a duplicate key
        val exception = assertThrows<IllegalArgumentException> {
            iniFile.addValue("section1", "key1", "value2")
        }
        assertTrue(exception.message!!.contains("Failed to add key 'key1' to section 'section1'"))
    }

    @Test
    fun `test non-existent key error`() {
        val iniFile = IniFile(emptyList())

        // Try to get a non-existent key
        val exception = assertThrows<NoSuchElementException> {
            iniFile.getStringValue("section1", "key1")
        }
        assertTrue(exception.message!!.contains("Section 'section1' not found"))

        // Add a section
        iniFile.addSection("section1")

        // Try to get a non-existent key from an existing section
        val keyException = assertThrows<NoSuchElementException> {
            iniFile.getStringValue("section1", "key1")
        }
        assertTrue(keyException.message!!.contains("Key 'key1' not found"))

        // Try to delete a non-existent key
        val deleteException = assertThrows<NoSuchElementException> {
            iniFile.deleteValue("section1", "key1")
        }
        assertTrue(deleteException.message!!.contains("Failed to delete key 'key1' from section 'section1'"))
    }

    @Test
    fun `test to string`() {
        // Create a simple INI file with multiple sections
        val iniFile = IniFile(emptyList())
        iniFile.addValue("section1", "key1", "value1")
        iniFile.addValue("section1", "key2", 123)
        iniFile.addValue("section2", "key1", true)
        // Convert to string and verify format
        val iniString = iniFile.toString()
        println(iniString)
        assertTrue(iniString.contains("[section1]"))
        assertTrue(iniString.contains("key1=value1"))
        assertTrue(iniString.contains("key2=123"))
        assertTrue(iniString.contains("[section2]"))
        assertTrue(iniString.contains("key1=True"))
    }
}
