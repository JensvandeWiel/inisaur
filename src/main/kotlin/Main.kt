
suspend fun main() {
    val section = IniSection("Test")
    section.addKey("key1", "value1")
    println(section.toString())

    section.updateKey("key1", "newValue")

    println(section.toString())

    section.createOrUpdateKey("key2", "value2")
    println(section.toString())

    section.removeKey("key1")
    println(section.toString())
}