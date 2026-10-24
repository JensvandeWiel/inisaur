package serialization

import annotations.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@IniSection("OmitSection")
data class OmitTestConfig(
    val required: String,
    
    @OmitIfNull
    val optional: String? = null,
    
    val nullableButIncluded: String? = null
)

@IniStruct
data class OmitStruct(
    val name: String,
    
    @OmitIfNull
    val details: String? = null
)

@IniSection("StructSection")
data class StructOmitConfig(
    val struct: OmitStruct
)

class OmitIfNullTest {

    @Test
    fun `null values with OmitIfNull are omitted from sections`() {
        val config = OmitTestConfig("present", null, null)
        val iniContent = IniSerializer.serializeSection(config)
        
        println("INI Content:\n$iniContent")
        
        assertTrue(iniContent.contains("required=present"), "Required field should be present")
        assertFalse(iniContent.contains("optional="), "Optional field with @OmitIfNull should be omitted")
        assertTrue(iniContent.contains("nullableButIncluded="), "Nullable field without @OmitIfNull should be included")
    }

    @Test
    fun `non-null values with OmitIfNull are included in sections`() {
        val config = OmitTestConfig("present", "I am here", null)
        val iniContent = IniSerializer.serializeSection(config)
        
        assertTrue(iniContent.contains("required=present"))
        assertTrue(iniContent.contains("optional=I am here"), "Optional field with @OmitIfNull should be included when not null")
    }

    @Test
    fun `null values with OmitIfNull are omitted from structs`() {
        val config = StructOmitConfig(OmitStruct("test", null))
        val iniContent = IniSerializer.serializeSection(config)
        
        println("INI Content with struct:\n$iniContent")
        
        assertTrue(iniContent.contains("struct=(name=test)"), "details field should be omitted from struct")
    }

    @Test
    fun `non-null values with OmitIfNull are included in structs`() {
        val config = StructOmitConfig(OmitStruct("test", "some details"))
        val iniContent = IniSerializer.serializeSection(config)
        
        assertTrue(iniContent.contains("struct=(details=some details,name=test)") || 
                   iniContent.contains("struct=(name=test, details=some details)"))
    }
}

