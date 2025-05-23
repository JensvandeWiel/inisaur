import enums.IniEntryType
import enums.IniValueType
import exceptions.InvalidTypeException
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class IniEntryTest {

    @Test
    fun testConstructorWithPlainValue() {
        val entry = IniEntry("key", IniValue("value"))
        assertEquals("key", entry.key)
        assertEquals(IniEntryType.Plain, entry.type)
        assertEquals("key=value", entry.toString())
    }

    @Test
    fun testConstructorWithList() {
        val list = listOf(IniValue("value1"), IniValue("value2"), IniValue("value3"))
        val entry = IniEntry("key", list, IniEntryType.CommaSeparatedArray)
        assertEquals("key", entry.key)
        assertEquals(IniEntryType.CommaSeparatedArray, entry.type)
        assertEquals("key=value1,value2,value3", entry.toString())
    }

    @Test
    fun testConstructorWithMap() {
        val map = mapOf("subkey1" to IniValue("value1"), "subkey2" to IniValue("value2"))
        val entry = IniEntry("key", map)
        assertEquals("key", entry.key)
        assertEquals(IniEntryType.Map, entry.type)
        assertEquals("key[subkey1]=value1\nkey[subkey2]=value2", entry.toString())
    }

    @Test
    fun testConstructorWithEmptyKey() {
        assertFailsWith<InvalidTypeException> {
            IniEntry("", IniValue("value"))
        }
    }

    @Test
    fun testInvalidTypeForList() {
        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue("value1"), IniValue("value2")), IniEntryType.Plain)
        }
    }

    @Test
    fun testInvalidTypeForMap() {
        assertFailsWith<InvalidTypeException> {
            IniEntry("key", mapOf("subkey" to IniValue("value")), IniEntryType.Plain)
        }
    }

    @Test
    fun testPlainValueToString() {
        val stringEntry = IniEntry("key", IniValue("value"))
        val booleanEntry = IniEntry("key", IniValue(true, false))
        val intEntry = IniEntry("key", IniValue(42))
        val floatEntry = IniEntry("key", IniValue(3.14f))

        assertEquals("key=value", stringEntry.toString())
        assertEquals("key=true", booleanEntry.toString())
        assertEquals("key=42", intEntry.toString())
        assertEquals("key=3.14", floatEntry.toString())
    }

    @Test
    fun testCommaSeparatedArrayToString() {
        val list = listOf(IniValue("value1"), IniValue("value2"), IniValue("value3"))
        val entry = IniEntry("key", list, IniEntryType.CommaSeparatedArray)

        assertEquals("key=value1,value2,value3", entry.toString())
    }

    @Test
    fun testRepeatedLineArrayToString() {
        val list = listOf(IniValue("value1"), IniValue("value2"), IniValue("value3"))
        val entry = IniEntry("key", list, IniEntryType.RepeatedLineArray)

        assertEquals("key=value1\nkey=value2\nkey=value3", entry.toString())
    }

    @Test
    fun testIndexedArrayToString() {
        val list = listOf(IniValue("value1"), IniValue("value2"), IniValue("value3"))
        val entry = IniEntry("key", list, IniEntryType.IndexedArray)

        // Note: The IndexedArray format adds newlines so we use trimIndent to normalize the expected result
        val expected = """
            key[0]=value1
            key[1]=value2
            key[2]=value3
        """.trimIndent()

        assertEquals(expected, entry.toString())
    }

    @Test
    fun testMapToString() {
        val map = mapOf("subkey1" to IniValue("value1"), "subkey2" to IniValue("value2"))
        val entry = IniEntry("key", map)

        assertEquals("key[subkey1]=value1\nkey[subkey2]=value2", entry.toString())
    }

    @Test
    fun testGetValue() = runBlocking<Unit> {
        val entry = IniEntry("key", IniValue("value"))
        assertEquals("value", entry.getValue()?.toString())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue("value")), IniEntryType.CommaSeparatedArray).getValue()
        }
    }

    @Test
    fun testGetBoolean() = runBlocking<Unit> {
        val entry = IniEntry("key", IniValue(true))
        assertTrue(entry.getBoolean()!!)

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", IniValue("not a boolean")).getBoolean()
        }

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue(true)), IniEntryType.CommaSeparatedArray).getBoolean()
        }
    }

    @Test
    fun testGetInteger() = runBlocking<Unit> {
        val entry = IniEntry("key", IniValue(42))
        assertEquals(42, entry.getInteger())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", IniValue("not an integer")).getInteger()
        }

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue(42)), IniEntryType.CommaSeparatedArray).getInteger()
        }
    }

    @Test
    fun testGetFloat() = runBlocking<Unit> {
        val entry = IniEntry("key", IniValue(3.14f))
        assertEquals(3.14f, entry.getFloat())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", IniValue("not a float")).getFloat()
        }

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue(3.14f)), IniEntryType.CommaSeparatedArray).getFloat()
        }
    }

    @Test
    fun testGetString() = runBlocking<Unit> {
        val entry = IniEntry("key", IniValue("value"))
        assertEquals("value", entry.getString())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", IniValue(42)).getString()
        }

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue("value")), IniEntryType.CommaSeparatedArray).getString()
        }
    }

    @Test
    fun testGetStruct() = runBlocking<Unit> {
        val struct = structOf("subkey" to "value")
        val entry = IniEntry("key", IniValue(struct))
        val result = entry.getStruct()

        assertNotNull(result)
        assertEquals("value", result["subkey"]?.toString())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", IniValue("not a struct")).getStruct()
        }

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue(struct)), IniEntryType.CommaSeparatedArray).getStruct()
        }
    }

    @Test
    fun testSetValue() = runBlocking<Unit> {
        val entry = IniEntry("key", IniValue("oldValue"))
        entry.setValue(IniValue("newValue"))
        assertEquals("newValue", entry.getValue()?.toString())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue("value")), IniEntryType.CommaSeparatedArray).setValue(IniValue("newValue"))
        }
    }

    @Test
    fun testSetBoolean() = runBlocking<Unit> {
        val entry = IniEntry("key", IniValue(false))
        entry.setBoolean(true)
        assertTrue(entry.getBoolean()!!)

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue(true)), IniEntryType.CommaSeparatedArray).setBoolean(false)
        }
    }

    @Test
    fun testSetInteger() = runBlocking<Unit> {
        val entry = IniEntry("key", IniValue(10))
        entry.setInteger(20)
        assertEquals(20, entry.getInteger())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue(10)), IniEntryType.CommaSeparatedArray).setInteger(20)
        }
    }

    @Test
    fun testSetFloat() = runBlocking<Unit> {
        val entry = IniEntry("key", IniValue(1.0f))
        entry.setFloat(2.0f)
        assertEquals(2.0f, entry.getFloat())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue(1.0f)), IniEntryType.CommaSeparatedArray).setFloat(2.0f)
        }
    }

    @Test
    fun testSetString() = runBlocking<Unit> {
        val entry = IniEntry("key", IniValue("oldValue"))
        entry.setString("newValue")
        assertEquals("newValue", entry.getString())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue("oldValue")), IniEntryType.CommaSeparatedArray).setString("newValue")
        }
    }

    @Test
    fun testSetStruct() = runBlocking<Unit> {
        val oldStruct = structOf("oldKey" to IniValue("oldValue"))
        val newStruct = structOf("newKey" to IniValue("newValue"))

        val entry = IniEntry("key", IniValue(oldStruct))
        entry.setStruct(newStruct)

        val result = entry.getStruct()
        assertNotNull(result)
        assertEquals("newValue", result["newKey"]?.toString())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue(oldStruct)), IniEntryType.CommaSeparatedArray).setStruct(newStruct)
        }
    }

    @Test
    fun testGetArrayValues() = runBlocking<Unit> {
        val list = listOf(IniValue("value1"), IniValue("value2"))
        val entry = IniEntry("key", list, IniEntryType.CommaSeparatedArray)
        val result = entry.getArrayValues()

        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("value1", result[0]?.toString())
        assertEquals("value2", result[1]?.toString())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", IniValue("not an array")).getArrayValues()
        }
    }

    @Test
    fun testSetArrayValues() = runBlocking<Unit> {
        val oldList = listOf(IniValue("oldValue1"), IniValue("oldValue2"))
        val newList = listOf(IniValue("newValue1"), IniValue("newValue2"), IniValue("newValue3"))

        val entry = IniEntry("key", oldList, IniEntryType.CommaSeparatedArray)
        entry.setArrayValues(newList)

        val result = entry.getArrayValues()
        assertNotNull(result)
        assertEquals(3, result.size)
        assertEquals("newValue1", result[0]?.toString())
        assertEquals("newValue2", result[1]?.toString())
        assertEquals("newValue3", result[2]?.toString())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", IniValue("not an array")).setArrayValues(newList)
        }
    }

    @Test
    fun testGetMapValues() = runBlocking<Unit> {
        val map = mapOf("subkey1" to IniValue("value1"), "subkey2" to IniValue("value2"))
        val entry = IniEntry("key", map)
        val result = entry.getMapValues()

        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("value1", result["subkey1"]?.toString())
        assertEquals("value2", result["subkey2"]?.toString())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", IniValue("not a map")).getMapValues()
        }
    }

    @Test
    fun testSetMapValues() = runBlocking<Unit> {
        val oldMap = mapOf("oldKey" to IniValue("oldValue"))
        val newMap = mapOf("newKey1" to IniValue("newValue1"), "newKey2" to IniValue("newValue2"))

        val entry = IniEntry("key", oldMap)
        entry.setMapValues(newMap)

        val result = entry.getMapValues()
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("newValue1", result["newKey1"]?.toString())
        assertEquals("newValue2", result["newKey2"]?.toString())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", IniValue("not a map")).setMapValues(newMap)
        }
    }

    @Test
    fun testEntryType() {
        val plainEntry = IniEntry("key", IniValue("value"))
        val arrayEntry = IniEntry("key", listOf(IniValue("value")), IniEntryType.CommaSeparatedArray)
        val mapEntry = IniEntry("key", mapOf("subkey" to IniValue("value")))

        assertEquals(IniEntryType.Plain, plainEntry.entryType())
        assertEquals(IniEntryType.CommaSeparatedArray, arrayEntry.entryType())
        assertEquals(IniEntryType.Map, mapEntry.entryType())
    }

    @Test
    fun testPlainEntryValueType() {
        val stringEntry = IniEntry("key", IniValue("value"))
        val booleanEntry = IniEntry("key", IniValue(true))
        val intEntry = IniEntry("key", IniValue(42))
        val floatEntry = IniEntry("key", IniValue(3.14f))
        val structEntry = IniEntry("key", IniValue(structOf("subkey" to IniValue("value"))))

        assertEquals(IniValueType.String, stringEntry.plainEntryValueType())
        assertEquals(IniValueType.CapitalizedBoolean, booleanEntry.plainEntryValueType())
        assertEquals(IniValueType.Integer, intEntry.plainEntryValueType())
        assertEquals(IniValueType.Float, floatEntry.plainEntryValueType())
        assertEquals(IniValueType.Struct, structEntry.plainEntryValueType())

        assertFailsWith<InvalidTypeException> {
            IniEntry("key", listOf(IniValue("value")), IniEntryType.CommaSeparatedArray).plainEntryValueType()
        }
    }

    @Test
    fun testNullValueHandling() = runBlocking<Unit> {
        // Test with null values in different contexts
        val nullString = IniEntry("key", IniValue(null as String?))
        assertEquals("key=", nullString.toString())

        val nullBoolean = IniEntry("key", IniValue(null as Boolean?))
        assertEquals("key=", nullBoolean.toString())

        val nullInteger = IniEntry("key", IniValue(null as Int?))
        assertEquals("key=", nullInteger.toString())

        val nullFloat = IniEntry("key", IniValue(null as Float?))
        assertEquals("key=", nullFloat.toString())

        val nullStruct = IniEntry("key", IniValue(null as Struct?))
        assertEquals("key=", nullStruct.toString())

        // Test with null elements in array
        val arrayWithNulls = IniEntry("key", listOf(IniValue("value1"), IniValue(null as String?), IniValue("value3")), IniEntryType.CommaSeparatedArray)
        assertEquals("key=value1,value3", arrayWithNulls.toString())

        // Test setting to null
        val entry = IniEntry("key", IniValue("value"))
        entry.setValue(IniValue(null as String?))
        assertNull(entry.getValue()?.getValue())
        assertEquals("key=", entry.toString())
    }

    @Test
    fun testArrayWithEmptyValues() {
        var commaSeparatedArrayWithEmptyValues = IniEntry("key", listOf("value1", IniValue(null as String?), "value3"), IniEntryType.CommaSeparatedArray)
        assertEquals("key=value1,value3", commaSeparatedArrayWithEmptyValues.toString())

        var repeatedLineArrayWithEmptyValues = IniEntry("key", listOf(IniValue("value1"), IniValue(null as String?), IniValue("value3")), IniEntryType.RepeatedLineArray)
        assertEquals("key=value1\nkey=value3", repeatedLineArrayWithEmptyValues.toString())

        val indexedArrayWithEmptyKeys = IniEntry("key", listOf(IniValue(null as String?), IniValue(1), IniValue(null as String?)), IniEntryType.IndexedArray)
        assertEquals("key[0]=\nkey[1]=1\nkey[2]=", indexedArrayWithEmptyKeys.toString())
    }
}


