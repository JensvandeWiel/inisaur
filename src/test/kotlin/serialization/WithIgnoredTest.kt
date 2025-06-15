package serialization

import IniFile
import annotations.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@IniSection("KnownSection")
data class ConfigWithIgnoredSection(
    val name: String,
    val value: Int
)

@IniSerializable
data class FullConfigWithIgnoredSections(
    val knownSection: ConfigWithIgnoredSection,
    override val ignoredKeys: IniFile = IniFile()
) : WithIgnored

class WithIgnoredTest {
    @Test
    fun `section should collect unknown keys`() {
        val ini = """
            [KnownSection]
            name=Test
            value=42
            unknownKey1=unknown value
            unknownKey2=123
        """.trimIndent()

        // Using deserialize instead of deserializeSection to get a FullConfigWithIgnoredSections instance
        val fullConfig = IniSerializer.deserialize<FullConfigWithIgnoredSections>(ini)

        // Check that known properties are properly deserialized
        assertEquals("Test", fullConfig.knownSection.name)
        assertEquals(42, fullConfig.knownSection.value)

        // Check that unknown keys are collected in the top-level ignoredKeys
        val ignoredSection = fullConfig.ignoredKeys.getSection("KnownSection")
        assertNotNull(ignoredSection)
        assertEquals("unknown value", ignoredSection.getStringKey("unknownKey1"))
        assertEquals(123, ignoredSection.getIntKey("unknownKey2"))
    }

    @Test
    fun `full config should collect unknown sections and keys`() {
        val ini = """
            [KnownSection]
            name=Test
            value=42
            unknownKey=some value
            
            [UnknownSection1]
            key1=value1
            key2=value2
            
            [UnknownSection2]
            setting=enabled
        """.trimIndent()

        val config = IniSerializer.deserialize<FullConfigWithIgnoredSections>(ini)

        // Check that known properties are properly deserialized
        assertEquals("Test", config.knownSection.name)
        assertEquals(42, config.knownSection.value)

        // Check that unknown sections are collected in the top-level ignoredKeys
        assertTrue(config.ignoredKeys.hasSection("UnknownSection1"))
        assertTrue(config.ignoredKeys.hasSection("UnknownSection2"))

        // Check values in unknown sections
        assertEquals("value1", config.ignoredKeys.getStringValue("UnknownSection1", "key1"))
        assertEquals("value2", config.ignoredKeys.getStringValue("UnknownSection1", "key2"))
        assertEquals("enabled", config.ignoredKeys.getStringValue("UnknownSection2", "setting"))

        // Check that unknown keys in known sections are also collected in the top-level ignoredKeys
        assertEquals("some value", config.ignoredKeys.getStringValue("KnownSection", "unknownKey"))
    }

    @Test
    fun `round-trip serialization should preserve all keys`() {
        val originalIni = """
            [KnownSection]
            name=Test
            value=42
            unknownKey1=unknown value
            unknownKey2=123
            
            [UnknownSection]
            key1=value1
            key2=value2
        """.trimIndent()

        // Deserialize with WithIgnored
        val config = IniSerializer.deserialize<FullConfigWithIgnoredSections>(originalIni)

        // Modify a known property
        val modifiedConfig = config.copy(
            knownSection = config.knownSection.copy(name = "Modified")
        )

        // Serialize back to INI
        val serialized = IniSerializer.serialize(modifiedConfig)

        // Check that known properties are updated
        assertTrue(serialized.contains("name=Modified"))
        assertTrue(serialized.contains("value=42"))

        // Check that unknown keys in known sections are preserved
        assertTrue(serialized.contains("unknownKey1=unknown value"))
        assertTrue(serialized.contains("unknownKey2=123"))

        // Check that unknown sections are preserved
        assertTrue(serialized.contains("[UnknownSection]"))
        assertTrue(serialized.contains("key1=value1"))
        assertTrue(serialized.contains("key2=value2"))
    }

    @Test
    fun `complex ignored values should be properly preserved`() {
        val ini = """
            [KnownSection]
            name=Test
            value=42
            
            [ComplexSection]
            arrayValue=val1,val2,val3
            indexedArray[0]=zero
            indexedArray[1]=one
            namedMap[key1]=value1
            namedMap[key2]=value2
            repeatedValue=first
            repeatedValue=second
            structValue=(x=10, y=20, z=30)
        """.trimIndent()

        val config = IniSerializer.deserialize<FullConfigWithIgnoredSections>(ini)

        // Check that complex values are preserved in the ignored section
        val complexSection = config.ignoredKeys.getSection("ComplexSection")

        // Check comma-separated array
        val arrayValue = complexSection.getArrayKey("arrayValue")
        assertEquals(3, arrayValue.size)
        assertEquals("val1", arrayValue[0])
        assertEquals("val2", arrayValue[1])
        assertEquals("val3", arrayValue[2])

        // Check indexed array
        val indexedArray = complexSection.getIndexedArrayKey("indexedArray")
        assertEquals(2, indexedArray.size)
        assertEquals("zero", indexedArray[0])
        assertEquals("one", indexedArray[1])

        // Check named map
        val namedMap = complexSection.getMapKey("namedMap")
        assertEquals(2, namedMap.size)
        assertEquals("value1", namedMap["key1"])
        assertEquals("value2", namedMap["key2"])

        // Check repeated values
        val repeatedValues = complexSection.getArrayKey("repeatedValue")
        assertEquals(2, repeatedValues.size)
        assertEquals("first", repeatedValues[0])
        assertEquals("second", repeatedValues[1])

        // Check struct values
        val structValue = complexSection.getStructKey("structValue")
        assertEquals(3, structValue.size)
        assertEquals(10, structValue["x"])
        assertEquals(20, structValue["y"])
        assertEquals(30, structValue["z"])
    }
}
