import exceptions.InvalidTypeException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SectionTest {

    @Test
    fun `test section creation and toString`() {
        val section = Section("TestSection")
        section.addKey("StringKey", "StringValue")
        section.addKey("IntKey", 42)

        val expectedString = "[TestSection]\nStringKey=StringValue\nIntKey=42"
        assertEquals(expectedString, section.toString())
    }

    @Test
    fun `test getKey and getStringKey`() {
        val section = Section("TestSection")
        section.addKey("StringKey", "StringValue")

        val value = section.getKey("StringKey")
        assertTrue(value is StringValue)
        assertEquals("StringValue", value.toString())
        assertEquals("StringValue", section.getStringKey("StringKey"))

        // Test non-existent key
        assertThrows<NoSuchElementException> { section.getKey("NonExistentKey") }
        assertThrows<NoSuchElementException> { section.getStringKey("NonExistentKey") }
    }

    @Test
    fun `test getIntKey`() {
        val section = Section("TestSection")
        section.addKey("IntKey", 42)

        assertEquals(42, section.getIntKey("IntKey"))

        // Test type mismatch
        section.addKey("StringKey", "NotAnInt")
        assertThrows<InvalidTypeException> { section.getIntKey("StringKey") }
    }

    @Test
    fun `test getFloatKey`() {
        val section = Section("TestSection")
        section.addKey("FloatKey", 3.14f)

        assertEquals(3.14f, section.getFloatKey("FloatKey"))

        // Test type mismatch
        section.addKey("StringKey", "NotAFloat")
        assertThrows<InvalidTypeException> { section.getFloatKey("StringKey") }
    }

    @Test
    fun `test getBooleanKey`() {
        val section = Section("TestSection")
        section.addKey("BoolKey", true)

        assertEquals(true, section.getBooleanKey("BoolKey"))

        // Test type mismatch
        section.addKey("StringKey", "NotABoolean")
        assertThrows<InvalidTypeException> { section.getBooleanKey("StringKey") }
    }

    @Test
    fun `test getStructKey`() {
        val section = Section("TestSection")
        val structMap = mapOf("Key1" to "Value1", "Key2" to 42)
        section.addKey("StructKey", structMap)

        val result = section.getStructKey("StructKey")
        assertEquals("Value1", result["Key1"])
        assertEquals(42, result["Key2"])

        // Test type mismatch
        section.addKey("StringKey", "NotAStruct")
        assertThrows<InvalidTypeException> { section.getStructKey("StringKey") }
    }

    @Test
    fun `test getArrayKey with CommaSeparatedArray`() {
        val section = Section("TestSection")
        val arrayValues = listOf("value1", "value2", "value3")
        section.addArrayKey("ArrayKey", arrayValues, ArrayType.CommaSeparatedArray)

        val result = section.getArrayKey("ArrayKey")
        assertEquals(3, result.size)
        assertEquals(arrayValues, result)

        // Test type mismatch
        section.addKey("StringKey", "NotAnArray")
        assertThrows<InvalidTypeException> { section.getArrayKey("StringKey") }
    }

    @Test
    fun `test getArrayKey with RepeatedLineArray`() {
        val section = Section("TestSection")
        val arrayValues = listOf("value1", "value2", "value3")
        section.addArrayKey("ArrayKey", arrayValues, ArrayType.RepeatedLineArray)

        val result = section.getArrayKey("ArrayKey")
        assertEquals(3, result.size)
        assertEquals(arrayValues, result)
    }

    @Test
    fun `test setKey for String value`() {
        val section = Section("TestSection")
        section.addKey("StringKey", "InitialValue")
        assertEquals("InitialValue", section.getStringKey("StringKey"))

        section.setKey("StringKey", "UpdatedValue")
        assertEquals("UpdatedValue", section.getStringKey("StringKey"))

        // Setting a new key
        section.setKey("NewStringKey", "NewValue")
        assertEquals("NewValue", section.getStringKey("NewStringKey"))
    }

    @Test
    fun `test setKey for Int value`() {
        val section = Section("TestSection")
        section.addKey("IntKey", 42)
        assertEquals(42, section.getIntKey("IntKey"))

        section.setKey("IntKey", 100)
        assertEquals(100, section.getIntKey("IntKey"))

        // Setting a new key
        section.setKey("NewIntKey", 200)
        assertEquals(200, section.getIntKey("NewIntKey"))
    }

    @Test
    fun `test setKey for Float value`() {
        val section = Section("TestSection")
        section.addKey("FloatKey", 3.14f)
        assertEquals(3.14f, section.getFloatKey("FloatKey"))

        section.setKey("FloatKey", 2.71f)
        assertEquals(2.71f, section.getFloatKey("FloatKey"))

        // Setting a new key
        section.setKey("NewFloatKey", 1.23f)
        assertEquals(1.23f, section.getFloatKey("NewFloatKey"))
    }

    @Test
    fun `test setKey for Boolean value`() {
        val section = Section("TestSection")
        section.addKey("BoolKey", true)
        assertEquals(true, section.getBooleanKey("BoolKey"))

        section.setKey("BoolKey", false)
        assertEquals(false, section.getBooleanKey("BoolKey"))

        // Setting a new key
        section.setKey("NewBoolKey", true)
        assertEquals(true, section.getBooleanKey("NewBoolKey"))

        // Test capitalization
        section.setKey("CapitalizedBoolKey", true, true)
        assertEquals("True", section.getKey("CapitalizedBoolKey").toString())
    }

    @Test
    fun `test setKey for Map value`() {
        val section = Section("TestSection")
        val initialMap = mapOf("Key1" to "Value1", "Key2" to 42)
        section.addKey("StructKey", initialMap)

        val updatedMap = mapOf("Key1" to "UpdatedValue", "Key3" to 100)
        section.setKey("StructKey", updatedMap)

        val result = section.getStructKey("StructKey")
        assertEquals("UpdatedValue", result["Key1"])
        assertEquals(100, result["Key3"])
        assertNull(result["Key2"]) // Key2 should not exist after update
    }

    @Test
    fun `test setArrayKey`() {
        val section = Section("TestSection")
        val initialArray = listOf("value1", "value2")
        section.addArrayKey("ArrayKey", initialArray)

        val updatedArray = listOf("updated1", "updated2", "updated3")
        section.setArrayKey("ArrayKey", updatedArray)

        val result = section.getArrayKey("ArrayKey")
        assertEquals(3, result.size)
        assertEquals(updatedArray, result)

        // Setting a new array key
        val newArray = listOf("new1", "new2")
        section.setArrayKey("NewArrayKey", newArray)
        val newResult = section.getArrayKey("NewArrayKey")
        assertEquals(2, newResult.size)
        assertEquals(newArray, newResult)

        // Test type mismatch
        section.addKey("StringKey", "NotAnArray")
        assertThrows<InvalidTypeException> { section.setArrayKey("StringKey", listOf("value")) }
    }

    @Test
    fun `test setIndexedArrayKey`() {
        val section = Section("TestSection")
        val initialMap = mapOf(0 to "value0", 2 to "value2")
        section.addIndexedArrayKey("IndexedArrayKey", initialMap)

        val updatedMap = mapOf(0 to "updated0", 1 to "value1", 2 to "updated2")
        section.setIndexedArrayKey("IndexedArrayKey", updatedMap)

        // Need to manually test entries for indexed array since there's no direct getter
        val indexedArrayEntry = section.toString()
        assertTrue(indexedArrayEntry.contains("IndexedArrayKey[0]=updated0"))
        assertTrue(indexedArrayEntry.contains("IndexedArrayKey[1]=value1"))
        assertTrue(indexedArrayEntry.contains("IndexedArrayKey[2]=updated2"))

        // Setting a new indexed array key
        val newMap = mapOf(5 to "value5", 10 to "value10")
        section.setIndexedArrayKey("NewIndexedArrayKey", newMap)
        val newResult = section.toString()
        assertTrue(newResult.contains("NewIndexedArrayKey[5]=value5"))
        assertTrue(newResult.contains("NewIndexedArrayKey[10]=value10"))

        // Test type mismatch
        section.addKey("StringKey", "NotAnIndexedArray")
        assertThrows<InvalidTypeException> { section.setIndexedArrayKey("StringKey", mapOf(0 to "value")) }
    }

    @Test
    fun `test setMapKey`() {
        val section = Section("TestSection")
        val initialMap = mapOf("SubKey1" to "Value1", "SubKey2" to "Value2")
        section.addMapKey("MapKey", initialMap)

        val updatedMap = mapOf("SubKey1" to "Updated1", "SubKey3" to "Value3")
        section.setMapKey("MapKey", updatedMap)

        // Need to verify entries in the string representation
        val mapEntryString = section.toString()
        assertTrue(mapEntryString.contains("MapKey[SubKey1]=Updated1"))
        assertTrue(mapEntryString.contains("MapKey[SubKey3]=Value3"))
        assertFalse(mapEntryString.contains("MapKey[SubKey2]=Value2")) // Should not contain old value

        // Setting a new map key
        val newMap = mapOf("NewKey1" to "NewValue1", "NewKey2" to "NewValue2")
        section.setMapKey("NewMapKey", newMap)
        val newResult = section.toString()
        assertTrue(newResult.contains("NewMapKey[NewKey1]=NewValue1"))
        assertTrue(newResult.contains("NewMapKey[NewKey2]=NewValue2"))

        // Test type mismatch
        section.addKey("StringKey", "NotAMap")
        assertThrows<InvalidTypeException> { section.setMapKey("StringKey", mapOf("Key" to "Value")) }
    }

    @Test
    fun `test deleteKey`() {
        val section = Section("TestSection")
        section.addKey("Key1", "Value1")
        section.addKey("Key2", "Value2")

        assertTrue(section.toString().contains("Key1=Value1"))
        assertTrue(section.toString().contains("Key2=Value2"))

        section.deleteKey("Key1")

        assertFalse(section.toString().contains("Key1=Value1"))
        assertTrue(section.toString().contains("Key2=Value2"))

        // Test deleting non-existent key
        assertThrows<NoSuchElementException> { section.deleteKey("NonExistentKey") }
    }

    @Test
    fun `test adding duplicate keys throws exception`() {
        val section = Section("TestSection")
        section.addKey("DuplicateKey", "Value1")

        // Adding the same key again should throw an exception
        assertThrows<IllegalArgumentException> { section.addKey("DuplicateKey", "Value2") }
        assertThrows<IllegalArgumentException> { section.addArrayKey("DuplicateKey", listOf("value")) }
        assertThrows<IllegalArgumentException> { section.addIndexedArrayKey("DuplicateKey", mapOf(0 to "value")) }
        assertThrows<IllegalArgumentException> { section.addMapKey("DuplicateKey", mapOf("Key" to "Value")) }
    }

    @Test
    fun `test handling of null values`() {
        val section = Section("TestSection")

        // Test adding null values
        section.addKey("NullString", null as String?)
        section.addKey("NullInt", null as Int?)
        section.addKey("NullFloat", null as Float?)
        section.addKey("NullBoolean", null as Boolean?)

        // Verify they're stored properly
        assertNull(section.getStringKey("NullString"))
        assertNull(section.getIntKey("NullInt"))
        assertNull(section.getFloatKey("NullFloat"))
        assertNull(section.getBooleanKey("NullBoolean"))
    }

    @Test
    fun `test nested struct values`() {
        val section = Section("TestSection")
        val nestedMap = mapOf(
            "Key1" to "Value1",
            "NestedStruct" to mapOf("SubKey1" to "SubValue1", "SubKey2" to 42)
        )

        section.addKey("StructWithNested", nestedMap)

        val result = section.getStructKey("StructWithNested")
        assertEquals("Value1", result["Key1"])

        @Suppress("UNCHECKED_CAST")
        val nestedResult = result["NestedStruct"] as Map<String, Any?>
        assertEquals("SubValue1", nestedResult["SubKey1"])
        assertEquals(42, nestedResult["SubKey2"])
    }

    @Test
    fun `test section with multiple types of values`() {
        val section = Section("ComplexSection")

        // Add different types of values
        section.addKey("StringKey", "StringValue")
        section.addKey("IntKey", 42)
        section.addKey("FloatKey", 3.14f)
        section.addKey("BoolKey", true)
        section.addKey("StructKey", mapOf("Key1" to "Value1", "Key2" to 42))
        section.addArrayKey("ArrayKey", listOf("value1", "value2", "value3"))
        section.addIndexedArrayKey("IndexedKey", mapOf(0 to "value0", 2 to "value2"))
        section.addMapKey("MapKey", mapOf("SubKey1" to "Value1", "SubKey2" to "Value2"))

        // Verify toString contains all entries
        val sectionString = section.toString()
        assertTrue(sectionString.contains("[ComplexSection]"))
        assertTrue(sectionString.contains("StringKey=StringValue"))
        assertTrue(sectionString.contains("IntKey=42"))
        assertTrue(sectionString.contains("FloatKey=3.14"))
        assertTrue(sectionString.contains("BoolKey=True"))
        assertTrue(sectionString.contains("StructKey=(Key1=Value1, Key2=42)"))
        assertTrue(sectionString.contains("ArrayKey=value1,value2,value3"))
        assertTrue(sectionString.contains("IndexedKey[0]=value0"))
        assertTrue(sectionString.contains("IndexedKey[2]=value2"))
        assertTrue(sectionString.contains("MapKey[SubKey1]=Value1"))
        assertTrue(sectionString.contains("MapKey[SubKey2]=Value2"))
    }
}
