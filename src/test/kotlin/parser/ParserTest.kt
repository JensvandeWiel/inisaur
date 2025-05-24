package parser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ParserTest {

    @Test
    fun `test parsing basic INI file`() {
        val input = """
            [ServerSettings]
            ServerName=ARK Server
            MaxPlayers=70
            EnablePvP=True
            DifficultyOffset=0.2
            CustomRecipeCostMultiplier=1.5
            WelcomeMessage=Welcome to the khjghjghjgjkh
            
            [Mods]
            ModList=123456,654321
            Ke1y=Value
            Ke1y=Value2
            Key[0]=16
            Key[1]=32
            Key[2]=64
            ModMap[Main]=Valguero
            ModMap[Event]=Ragnarok
            OverrideNamedEngramEntries=(EngramClassName=EngramEntry_Dino_Aid_X_C,EngramHidden=True)
            NestedStruct=(StructName=NestedStruct,StructValue=(Key1=Value1,Key2=Value2))
        """.trimIndent()

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val iniFile = parser.parse()

        // Test overall structure
        assertEquals(2, iniFile.sections.size)
        assertEquals("ServerSettings", iniFile.sections[0].name)
        assertEquals("Mods", iniFile.sections[1].name)

        // Test ServerSettings section
        val serverSettings = iniFile.sections[0]
        assertEquals(6, serverSettings.entries.size)

        // Test specific entries in ServerSettings
        val serverName = serverSettings.entries[0] as Plain
        assertEquals("ServerName", serverName.key)
        assertTrue(serverName.value is StringValue)
        assertEquals("ARK Server", (serverName.value as StringValue).value)

        val maxPlayers = serverSettings.entries[1] as Plain
        assertEquals("MaxPlayers", maxPlayers.key)
        assertTrue(maxPlayers.value is IntValue)
        assertEquals(70, (maxPlayers.value as IntValue).value)

        val enablePvP = serverSettings.entries[2] as Plain
        assertEquals("EnablePvP", enablePvP.key)
        assertTrue(enablePvP.value is BoolValue)
        assertEquals(true, (enablePvP.value as BoolValue).value)
        assertEquals(true, (enablePvP.value as BoolValue).capitalized)

        val difficultyOffset = serverSettings.entries[3] as Plain
        assertEquals("DifficultyOffset", difficultyOffset.key)
        assertTrue(difficultyOffset.value is FloatValue)
        assertEquals(0.2f, (difficultyOffset.value as FloatValue).value)

        // Test Mods section
        val mods = iniFile.sections[1]
        assertEquals(6, mods.entries.size)

        // Test ModList comma separated array
        val modList = mods.entries[0] as CommaSeparatedArray
        assertEquals("ModList", modList.key)
        assertEquals(2, modList.values.size)
        assertTrue(modList.values[0] is IntValue)
        assertEquals(123456, (modList.values[0] as IntValue).value)
        assertEquals(654321, (modList.values[1] as IntValue).value)

        // Test Ke1y repeated line array
        val ke1y = mods.entries[1] as RepeatedLineArray
        assertEquals("Ke1y", ke1y.key)
        assertEquals(2, ke1y.values.size)
        assertEquals("Value", (ke1y.values[0] as StringValue).value)
        assertEquals("Value2", (ke1y.values[1] as StringValue).value)

        // Test Key indexed array
        val keyArray = mods.entries[2] as IndexedArray
        assertEquals("Key", keyArray.key)
        assertEquals(3, keyArray.indexedValues.size)
        assertEquals(16, (keyArray.indexedValues[0] as IntValue).value)
        assertEquals(32, (keyArray.indexedValues[1] as IntValue).value)
        assertEquals(64, (keyArray.indexedValues[2] as IntValue).value)

        // Test ModMap entry
        val modMap = mods.entries[3] as MapEntry
        assertEquals("ModMap", modMap.key)
        assertEquals(2, modMap.value.size)
        assertEquals("Valguero", (modMap.value["Main"] as StringValue).value)
        assertEquals("Ragnarok", (modMap.value["Event"] as StringValue).value)

        // Test structs
        val overrideNamedEngramEntries = mods.entries[4] as Plain
        assertTrue(overrideNamedEngramEntries.value is StructValue)
        val structFields = (overrideNamedEngramEntries.value as StructValue).fields
        assertEquals(2, structFields.size)
        assertEquals("EngramEntry_Dino_Aid_X_C", structFields["EngramClassName"])
        assertEquals(true, structFields["EngramHidden"])

        // Test nested struct
        val nestedStruct = mods.entries[5] as Plain
        assertTrue(nestedStruct.value is StructValue)
        val nestedStructFields = (nestedStruct.value as StructValue).fields
        assertEquals(2, nestedStructFields.size)
        assertEquals("NestedStruct", nestedStructFields["StructName"])

        @Suppress("UNCHECKED_CAST")
        val innerStruct = nestedStructFields["StructValue"] as Map<String, Any?>
        assertEquals(2, innerStruct.size)
        assertEquals("Value1", innerStruct["Key1"])
        assertEquals("Value2", innerStruct["Key2"])
    }

    @Test
    fun `test handling of whitespace and comments`() {
        val input = """
            ; This is a comment
            [Section]
            ; Comment within section
                KeyWithSpaceBefore=Value
            KeyWithSpaceAfter = Value
            KeyWithSpaceAround = Value with spaces   
            
              ; Comment after blank line
            Key=Value; Inline comment
        """.trimIndent()

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val iniFile = parser.parse()

        assertEquals(1, iniFile.sections.size)
        val section = iniFile.sections[0]
        assertEquals("Section", section.name)
        assertEquals(4, section.entries.size)

        // Check keys are trimmed
        assertEquals("KeyWithSpaceBefore", section.entries[0].key)
        assertEquals("KeyWithSpaceAfter", section.entries[1].key)
        assertEquals("KeyWithSpaceAround", section.entries[2].key)

        // Check values
        val entry3 = section.entries[3] as Plain
        assertEquals("Key", entry3.key)
        assertEquals("Value", (entry3.value as StringValue).value)
    }

    @Test
    fun `test error handling`() {
        val input = """
            [Incomplete Section
            Key=Value
        """.trimIndent()

        // The exception happens in the Lexer since it's called
        // in the Parser constructor
        val lexer = Lexer(input)

        assertThrows(IllegalArgumentException::class.java) {
            lexer.nextToken() // This will throw when reading the section header
        }
    }

    @Test
    fun `test error handling in parser`() {
        // Create an input with an invalid line that will trigger an exception
        val input = """
            [ValidSection]
            Key=Value
            InvalidLine
        """.trimIndent()

        val lexer = Lexer(input)
        val parser = Parser(lexer)

        // The parser throws an IllegalArgumentException when it encounters a key with no equals sign
        val exception = assertThrows(IllegalArgumentException::class.java) {
            parser.parse()
        }

        // Verify the exception message matches what we expect
        assertTrue(exception.message?.contains("Expected EQUALS") ?: false)
    }

    @Test
    fun `test empty file`() {
        val lexer = Lexer("")
        val parser = Parser(lexer)
        val iniFile = parser.parse()

        assertTrue(iniFile.sections.isEmpty())
    }

    @Test
    fun `test quoted strings`() {
        val input = """
            [Section]
            QuotedString="This is a quoted string"
            QuotedWithEscapes="This has \"quotes\" inside"
            QuotedWithNewline="Line 1\nLine 2"
        """.trimIndent()

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val iniFile = parser.parse()

        val section = iniFile.sections[0]
        assertEquals(3, section.entries.size)

        val entry1 = section.entries[0] as Plain
        assertEquals("This is a quoted string", (entry1.value as StringValue).value)

        val entry2 = section.entries[1] as Plain
        assertEquals("This has \"quotes\" inside", (entry2.value as StringValue).value)

        val entry3 = section.entries[2] as Plain
        assertEquals("Line 1\nLine 2", (entry3.value as StringValue).value)
    }

    @Test
    fun `test multiple sections`() {
        val input = """
            [Section1]
            Key1=Value1
            
            [Section2]
            Key2=Value2
            
            [Section3]
            Key3=Value3
        """.trimIndent()

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val iniFile = parser.parse()

        assertEquals(3, iniFile.sections.size)
        assertEquals("Section1", iniFile.sections[0].name)
        assertEquals("Section2", iniFile.sections[1].name)
        assertEquals("Section3", iniFile.sections[2].name)

        assertEquals(1, iniFile.sections[0].entries.size)
        assertEquals(1, iniFile.sections[1].entries.size)
        assertEquals(1, iniFile.sections[2].entries.size)
    }
}
