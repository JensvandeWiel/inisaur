import enums.IniEntryType
import kotlinx.coroutines.runBlocking
import writer.IniSection
import writer.Struct
import kotlin.test.*

class IniSectionTest {

    @Test
    fun testConstructor() {
        val section = IniSection("TestSection")
        assertEquals("TestSection", section.name)
        assertTrue(section.entries.isEmpty())
        assertTrue(section.isEmpty())
    }

    @Test
    fun testToString() {
        val section = IniSection("TestSection")
        section.addKey("key", "value")
        assertEquals("[TestSection]\nkey=value", section.toString())

        val emptySection = IniSection("EmptySection")
        assertEquals("[EmptySection]", emptySection.toString().trim())
    }

    @Test
    fun testAddGetBooleanKey() = runBlocking {
        val section = IniSection("TestSection")

        // Test with normal boolean
        section.addKey("bool1", true)
        assertEquals(true, section.getBooleanKey("bool1"))

        // Test with capitalized boolean
        section.addKey("bool2", true, capitalized = true)
        assertEquals(true, section.getBooleanKey("bool2"))

        // Test with lowercase boolean
        section.addKey("bool3", false, capitalized = false)
        assertEquals(false, section.getBooleanKey("bool3"))

        // Test with null value
        section.addKey("nullBool", null as Boolean?)
        assertNull(section.getBooleanKey("nullBool"))

        // Test get non-existent key
        assertNull(section.getBooleanKey("nonExistent"))
    }

    @Test
    fun testAddGetIntegerKey() = runBlocking {
        val section = IniSection("TestSection")

        section.addKey("int1", 42)
        assertEquals(42, section.getIntegerKey("int1"))

        section.addKey("int2", -100)
        assertEquals(-100, section.getIntegerKey("int2"))

        section.addKey("nullInt", null as Int?)
        assertNull(section.getIntegerKey("nullInt"))

        assertNull(section.getIntegerKey("nonExistent"))
    }

    @Test
    fun testAddGetFloatKey() = runBlocking {
        val section = IniSection("TestSection")

        section.addKey("float1", 3.14f)
        assertEquals(3.14f, section.getFloatKey("float1"))

        section.addKey("float2", -0.5f)
        assertEquals(-0.5f, section.getFloatKey("float2"))

        section.addKey("nullFloat", null as Float?)
        assertNull(section.getFloatKey("nullFloat"))

        assertNull(section.getFloatKey("nonExistent"))
    }

    @Test
    fun testAddGetStringKey() = runBlocking {
        val section = IniSection("TestSection")

        section.addKey("string1", "Hello, World!")
        assertEquals("Hello, World!", section.getStringKey("string1"))

        section.addKey("string2", "")
        assertEquals("", section.getStringKey("string2"))

        section.addKey("nullString", null as String?)
        assertNull(section.getStringKey("nullString"))

        assertNull(section.getStringKey("nonExistent"))
    }

    @Test
    fun testAddGetStructKey() = runBlocking {
        val section = IniSection("TestSection")

        val struct = mapOf("name" to "John", "age" to 30)
        section.addKey("person", struct)

        val retrieved = section.getStructKey("person")
        assertNotNull(retrieved)
        assertEquals("John", retrieved["name"])
        assertEquals(30, retrieved["age"])

        section.addKey("nullStruct", null as Struct?)
        assertNull(section.getStructKey("nullStruct"))

        assertNull(section.getStructKey("nonExistent"))
    }

    @Test
    fun testAddGetArrayKey() = runBlocking {
        val section = IniSection("TestSection")

        // Test comma separated array
        val commaArray = listOf("apple", "banana", "cherry")
        section.addArrayKey("fruits", commaArray, IniEntryType.CommaSeparatedArray)

        val retrievedComma = section.getArrayKey("fruits")
        assertNotNull(retrievedComma)
        assertEquals(3, retrievedComma.size)
        assertEquals("apple", retrievedComma[0])
        assertEquals("banana", retrievedComma[1])
        assertEquals("cherry", retrievedComma[2])

        // Test indexed array
        val indexedArray = listOf(10, 20, 30)
        section.addArrayKey("numbers", indexedArray, IniEntryType.IndexedArray)

        val retrievedIndexed = section.getArrayKey("numbers")
        assertNotNull(retrievedIndexed)
        assertEquals(3, retrievedIndexed.size)
        assertEquals(10, retrievedIndexed[0])
        assertEquals(20, retrievedIndexed[1])
        assertEquals(30, retrievedIndexed[2])

        // Test repeated line array
        val repeatedArray = listOf(true, false, true)
        section.addArrayKey("flags", repeatedArray, IniEntryType.RepeatedLineArray)

        val retrievedRepeated = section.getArrayKey("flags")
        assertNotNull(retrievedRepeated)
        assertEquals(3, retrievedRepeated.size)
        assertEquals(true, retrievedRepeated[0])
        assertEquals(false, retrievedRepeated[1])
        assertEquals(true, retrievedRepeated[2])

        // Test mixed types array
        val mixedArray = listOf("text", 42, 3.14f, true)
        section.addArrayKey("mixed", mixedArray, IniEntryType.CommaSeparatedArray)

        val retrievedMixed = section.getArrayKey("mixed")
        assertNotNull(retrievedMixed)
        assertEquals(4, retrievedMixed.size)
        assertEquals("text", retrievedMixed[0])
        assertEquals(42, retrievedMixed[1])
        assertEquals(3.14f, retrievedMixed[2])
        assertEquals(true, retrievedMixed[3])

        // Test with null values in array
        val nullableArray = listOf("text", null, "more text")
        section.addArrayKey("nullables", nullableArray, IniEntryType.CommaSeparatedArray)

        val retrievedNullable = section.getArrayKey("nullables")
        assertNotNull(retrievedNullable)
        assertEquals(3, retrievedNullable.size)
        assertEquals("text", retrievedNullable[0])
        assertNull(retrievedNullable[1])
        assertEquals("more text", retrievedNullable[2])

        assertNull(section.getArrayKey("nonExistent"))
    }

    @Test
    fun testAddGetMapKey() = runBlocking {
        val section = IniSection("TestSection")

        val map = mapOf(
            "name" to "John",
            "age" to 30,
            "isActive" to true,
            "height" to 1.85f
        )

        section.addMapKey("user", map)

        val retrieved = section.getMapKey("user")
        assertNotNull(retrieved)
        assertEquals(4, retrieved.size)
        assertEquals("John", retrieved["name"])
        assertEquals(30, retrieved["age"])
        assertEquals(true, retrieved["isActive"])
        assertEquals(1.85f, retrieved["height"])

        // Test with nested map
        val nestedMap = mapOf(
            "name" to "Jane",
            "address" to mapOf(
                "city" to "New York",
                "zip" to 10001
            )
        )

        section.addMapKey("userWithAddress", nestedMap)

        val retrievedNested = section.getMapKey("userWithAddress")
        assertNotNull(retrievedNested)
        assertEquals(2, retrievedNested.size)
        assertEquals("Jane", retrievedNested["name"])

        @Suppress("UNCHECKED_CAST")
        val nestedAddress = retrievedNested["address"] as Map<String, Any?>
        assertEquals("New York", nestedAddress["city"])
        assertEquals(10001, nestedAddress["zip"])

        // Test with null values in map
        val nullableMap = mapOf(
            "name" to "Smith",
            "middleName" to null,
            "age" to 25
        )

        section.addMapKey("nullableUser", nullableMap)

        val retrievedNullable = section.getMapKey("nullableUser")
        assertNotNull(retrievedNullable)
        assertEquals(3, retrievedNullable.size)
        assertEquals("Smith", retrievedNullable["name"])
        assertNull(retrievedNullable["middleName"])
        assertEquals(25, retrievedNullable["age"])

        assertNull(section.getMapKey("nonExistent"))
    }

    @Test
    fun testGetKeyType() {
        val section = IniSection("TestSection")

        section.addKey("string", "value")
        assertEquals(IniEntryType.Plain, section.getKeyType("string"))

        section.addArrayKey("array", listOf("a", "b"), IniEntryType.CommaSeparatedArray)
        assertEquals(IniEntryType.CommaSeparatedArray, section.getKeyType("array"))

        section.addMapKey("map", mapOf("k" to "v"))
        assertEquals(IniEntryType.Map, section.getKeyType("map"))

        assertNull(section.getKeyType("nonExistent"))
    }

    @Test
    fun testGetKey() = runBlocking {
        val section = IniSection("TestSection")

        section.addKey("string", "value")
        assertEquals("value", section.getKey("string"))

        section.addKey("int", 42)
        assertEquals(42, section.getKey("int"))

        section.addKey("bool", true)
        assertEquals(true, section.getKey("bool"))

        assertNull(section.getKey("nonExistent"))
    }

    @Test
    fun testUpdateKey() = runBlocking {
        val section = IniSection("TestSection")

        // Test updating boolean
        section.addKey("bool", true)
        assertEquals(true, section.getBooleanKey("bool"))

        section.updateKey("bool", false)
        assertEquals(false, section.getBooleanKey("bool"))

        // Test updating integer
        section.addKey("int", 10)
        assertEquals(10, section.getIntegerKey("int"))

        section.updateKey("int", 20)
        assertEquals(20, section.getIntegerKey("int"))

        // Test updating float
        section.addKey("float", 1.5f)
        assertEquals(1.5f, section.getFloatKey("float"))

        section.updateKey("float", 2.5f)
        assertEquals(2.5f, section.getFloatKey("float"))

        // Test updating string
        section.addKey("string", "old")
        assertEquals("old", section.getStringKey("string"))

        section.updateKey("string", "new")
        assertEquals("new", section.getStringKey("string"))

        // Test updating struct
        section.addKey("struct", mapOf("name" to "John"))
        val struct1 = section.getStructKey("struct")
        assertNotNull(struct1)
        assertEquals("John", struct1["name"])

        section.updateKey("struct", mapOf("name" to "Jane"))
        val struct2 = section.getStructKey("struct")
        assertNotNull(struct2)
        assertEquals("Jane", struct2["name"])

        // Test updating to null
        section.updateKey("string", null as String?)
        assertNull(section.getStringKey("string"))

        // Test exception for non-existent key
        assertFailsWith<IllegalArgumentException> {
            section.updateKey("nonExistent", "value")
        }
    }

    @Test
    fun testUpdateArrayKey() = runBlocking {
        val section = IniSection("TestSection")

        section.addArrayKey("fruits", listOf("apple", "banana"), IniEntryType.CommaSeparatedArray)
        val array1 = section.getArrayKey("fruits")
        assertNotNull(array1)
        assertEquals(2, array1.size)

        section.updateArrayKey("fruits", listOf("orange", "grape", "melon"))
        val array2 = section.getArrayKey("fruits")
        assertNotNull(array2)
        assertEquals(3, array2.size)
        assertEquals("orange", array2[0])
        assertEquals("grape", array2[1])
        assertEquals("melon", array2[2])

        // Test exception for non-existent key
        assertFailsWith<IllegalArgumentException> {
            section.updateArrayKey("nonExistent", listOf("value"))
        }
    }

    @Test
    fun testUpdateMapKey() = runBlocking {
        val section = IniSection("TestSection")

        section.addMapKey("user", mapOf("name" to "John", "age" to 30))
        val map1 = section.getMapKey("user")
        assertNotNull(map1)
        assertEquals(2, map1.size)

        section.updateMapKey("user", mapOf("name" to "Jane", "age" to 28, "city" to "New York"))
        val map2 = section.getMapKey("user")
        assertNotNull(map2)
        assertEquals(3, map2.size)
        assertEquals("Jane", map2["name"])
        assertEquals(28, map2["age"])
        assertEquals("New York", map2["city"])

        // Test exception for non-existent key
        assertFailsWith<IllegalArgumentException> {
            section.updateMapKey("nonExistent", mapOf("key" to "value"))
        }
    }

    @Test
    fun testDeleteKey() {
        val section = IniSection("TestSection")

        section.addKey("key", "value")
        assertTrue(section.containsKey("key"))

        section.deleteKey("key")
        assertFalse(section.containsKey("key"))

        // Test exception for non-existent key
        assertFailsWith<IllegalArgumentException> {
            section.deleteKey("nonExistent")
        }
    }

    @Test
    fun testClear() {
        val section = IniSection("TestSection")

        section.addKey("key1", "value1")
        section.addKey("key2", "value2")
        assertFalse(section.isEmpty())
        assertEquals(2, section.entries.size)

        section.clear()
        assertTrue(section.isEmpty())
        assertEquals(0, section.entries.size)
    }

    @Test
    fun testContainsKey() {
        val section = IniSection("TestSection")

        assertFalse(section.containsKey("key"))

        section.addKey("key", "value")
        assertTrue(section.containsKey("key"))

        section.deleteKey("key")
        assertFalse(section.containsKey("key"))
    }

    @Test
    fun testCreateOrUpdateKey() = runBlocking {
        val section = IniSection("TestSection")

        // Test create new boolean key
        section.createOrUpdateKey("bool", true)
        assertEquals(true, section.getBooleanKey("bool"))

        // Test update existing boolean key
        section.createOrUpdateKey("bool", false)
        assertEquals(false, section.getBooleanKey("bool"))

        // Test create new integer key
        section.createOrUpdateKey("int", 10)
        assertEquals(10, section.getIntegerKey("int"))

        // Test update existing integer key
        section.createOrUpdateKey("int", 20)
        assertEquals(20, section.getIntegerKey("int"))

        // Test create new float key
        section.createOrUpdateKey("float", 1.5f)
        assertEquals(1.5f, section.getFloatKey("float"))

        // Test update existing float key
        section.createOrUpdateKey("float", 2.5f)
        assertEquals(2.5f, section.getFloatKey("float"))

        // Test create new string key
        section.createOrUpdateKey("string", "new")
        assertEquals("new", section.getStringKey("string"))

        // Test update existing string key
        section.createOrUpdateKey("string", "updated")
        assertEquals("updated", section.getStringKey("string"))

        // Test create new struct key
        section.createOrUpdateKey("struct", mapOf("name" to "John"))
        val struct1 = section.getStructKey("struct")
        assertNotNull(struct1)
        assertEquals("John", struct1["name"])

        // Test update existing struct key
        section.createOrUpdateKey("struct", mapOf("name" to "Jane"))
        val struct2 = section.getStructKey("struct")
        assertNotNull(struct2)
        assertEquals("Jane", struct2["name"])
    }

    @Test
    fun testCreateOrUpdateArrayKey() = runBlocking {
        val section = IniSection("TestSection")

        // Test create new array key
        val array1 = listOf("apple", "banana")
        section.createOrUpdateArrayKey("fruits", array1)
        val retrieved1 = section.getArrayKey("fruits")
        assertNotNull(retrieved1)
        assertEquals(2, retrieved1.size)
        assertEquals("apple", retrieved1[0])
        assertEquals("banana", retrieved1[1])

        // Test update existing array key
        val array2 = listOf("orange", "grape", "melon")
        section.createOrUpdateArrayKey("fruits", array2)
        val retrieved2 = section.getArrayKey("fruits")
        assertNotNull(retrieved2)
        assertEquals(3, retrieved2.size)
        assertEquals("orange", retrieved2[0])
        assertEquals("grape", retrieved2[1])
        assertEquals("melon", retrieved2[2])
    }

    @Test
    fun testCreateOrUpdateMapKey() = runBlocking {
        val section = IniSection("TestSection")

        // Test create new map key
        val map1 = mapOf("name" to "John", "age" to 30)
        section.createOrUpdateMapKey("user", map1)
        val retrieved1 = section.getMapKey("user")
        assertNotNull(retrieved1)
        assertEquals(2, retrieved1.size)
        assertEquals("John", retrieved1["name"])
        assertEquals(30, retrieved1["age"])

        // Test update existing map key
        val map2 = mapOf("name" to "Jane", "age" to 28, "city" to "New York")
        section.createOrUpdateMapKey("user", map2)
        val retrieved2 = section.getMapKey("user")
        assertNotNull(retrieved2)
        assertEquals(3, retrieved2.size)
        assertEquals("Jane", retrieved2["name"])
        assertEquals(28, retrieved2["age"])
        assertEquals("New York", retrieved2["city"])
    }

    @Test
    fun testRemoveKeyOrFail() {
        val section = IniSection("TestSection")

        section.addKey("key", "value")
        assertTrue(section.containsKey("key"))

        section.removeKeyOrFail("key")
        assertFalse(section.containsKey("key"))

        // Test exception for non-existent key
        assertFailsWith<IllegalArgumentException> {
            section.removeKeyOrFail("nonExistent")
        }
    }

    @Test
    fun testRemoveKey() {
        val section = IniSection("TestSection")

        section.addKey("key", "value")
        assertTrue(section.containsKey("key"))

        // Test removing existing key
        assertTrue(section.removeKey("key"))
        assertFalse(section.containsKey("key"))

        // Test removing non-existent key
        assertFalse(section.removeKey("nonExistent"))
    }

    @Test
    fun testInvalidArrayType() {
        val section = IniSection("TestSection")

        // Test exception for invalid array type
        assertFailsWith<IllegalArgumentException> {
            section.addArrayKey("invalid", listOf("a", "b"), IniEntryType.Plain)
        }

        assertFailsWith<IllegalArgumentException> {
            section.addArrayKey("invalid", listOf("a", "b"), IniEntryType.Map)
        }
    }
}

