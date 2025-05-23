import enums.IniValueType
import kotlin.test.Test
import kotlin.test.assertEquals

class IniValueTest {
    @Test
    fun testToString() {
        val string = IniValue("Hello")
        val boolean = IniValue(true, false)
        val integer = IniValue(42)
        val float = IniValue(3.14f)
        val struct = IniValue(structOf("key" to IniValue("value")))
        val capitalizedBoolean = IniValue(true)

        assertEquals("Hello", string.toString())
        assertEquals("true", boolean.toString())
        assertEquals("42", integer.toString())
        assertEquals("3.14", float.toString())
        assertEquals("(key=value)", struct.toString())
        assertEquals("True", capitalizedBoolean.toString())
    }

    @Test
    fun testGetValue() {
        val string = IniValue("Hello")
        val boolean = IniValue(true, false)
        val integer = IniValue(42)
        val float = IniValue(3.14f)
        val struct = IniValue(structOf("key" to IniValue("value")))
        val capitalizedBoolean = IniValue(true)

        assertEquals("Hello", string.getValue())
        assertEquals(true, boolean.getValue())
        assertEquals(42, integer.getValue())
        assertEquals(3.14f, float.getValue())
        assertEquals(structOf("key" to IniValue("value")), struct.getValue())
        assertEquals(true, capitalizedBoolean.getValue())
    }

    @Test
    fun testType() {
        val string = IniValue("Hello")
        val boolean = IniValue(true, false)
        val integer = IniValue(42)
        val float = IniValue(3.14f)
        val struct = IniValue(structOf("key" to IniValue("value")))
        val capitalizedBoolean = IniValue(true)

        assertEquals(IniValueType.String, string.type())
        assertEquals(IniValueType.Boolean, boolean.type())
        assertEquals(IniValueType.Integer, integer.type())
        assertEquals(IniValueType.Float, float.type())
        assertEquals(IniValueType.Struct, struct.type())
        assertEquals(IniValueType.CapitalizedBoolean, capitalizedBoolean.type())
    }
}