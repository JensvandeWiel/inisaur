package serialization

import IniFile
import annotations.IniSection
import annotations.IniSerializable
import annotations.IniString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IniStringTest {

    @IniSerializable
    data class QuotedConfig(
        @IniSection
        val section: QuotedSection = QuotedSection()
    )

    @IniSection
    data class QuotedSection(
        @IniString(quoted = true)
        val quotedString: String = "simple",

        @IniString(quoted = false)
        val unquotedString: String = "simple",

        val defaultString: String = "simple"
    )

    @Test
    fun `test IniString quoted attribute`() {
        val config = QuotedConfig()
        val ini = IniSerializer.serialize(config)
        
        // Use contains to be independent of property order
        assert(ini.contains("quotedString=\"simple\""))
        assert(ini.contains("unquotedString=simple"))
        assert(ini.contains("defaultString=simple"))
        assert(ini.contains("[QuotedSection]"))
    }

    @Test
    fun `test IniString with special characters always quoted`() {
        @IniSection
        data class SpecialSection(
            @IniString(quoted = false)
            val specialString: String = "value;with;semicolon"
        )

        @IniSerializable
        data class SpecialConfig(
            @IniSection
            val section: SpecialSection = SpecialSection()
        )
        
        val config = SpecialConfig()
        val ini = IniSerializer.serialize(config)
        
        val expected = """
            [SpecialSection]
            specialString="value;with;semicolon"
        """.trimIndent()
        
        assertEquals(expected, ini.trim())
    }

    @Test
    fun `test manual addValue with quoted flag`() {
        val iniFile = IniFile()
        iniFile.addValue("Section", "quoted", "value", quoted = true)
        iniFile.addValue("Section", "unquoted", "value", quoted = false)
        
        val expected = """
            [Section]
            quoted="value"
            unquoted=value
        """.trimIndent()
        
        assertEquals(expected, iniFile.toString().trim())
    }
}
