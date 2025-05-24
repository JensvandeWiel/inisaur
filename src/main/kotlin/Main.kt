fun main() {
    val iniSection = Section("ExampleSection")
    iniSection.addKey("stringKey", "stringValue")
    iniSection.addKey("intKey", 42)
    iniSection.addKey("floatKey", 3.14f)
    iniSection.addKey("boolKey", true, capitalized = false)
    iniSection.addKey("structKey", mapOf("nestedKey" to "nestedValue"))
    iniSection.addKey("nullKey", null as String?)
    iniSection.addKey("emptyStructKey", emptyMap<String, Any>())
    iniSection.addIndexedArrayKey("indexedArrayKey", mapOf(1 to "first", 2 to "second", 4 to "fourth"))
    iniSection.addArrayKey("arrayKey", listOf("item1", "item2", "item3"))
    iniSection.addArrayKey("repeatedArrayKey", listOf("itemA", "itemB", "itemC"), ArrayType.RepeatedLineArray)
    iniSection.addMapKey("mapKey", mapOf("key1" to "value1", "key2" to 42))
    iniSection.addMapKey("nestedMapKey", mapOf("nestedKey1" to "nestedValue1", "nestedKey2" to 123))
    iniSection.addKey("boolCapitalizedKey", true, capitalized = true)
    println(iniSection)
    iniSection.setKey("stringKey", "newValue")
    iniSection.setKey("emptyStructKey", mapOf("newKey" to "newValue"))
    println("After updating keys:")
    println(iniSection)
}

fun displayKeyType(section: Section, key: String) {
    try {
        val value = section.getKey(key)
        println("$key: ${value::class.java.simpleName} (${value::class.qualifiedName})")
    } catch (e: Exception) {
        println("$key: Error - ${e.message}")
    }
}

fun displayMapTypes(map: Map<String, Any?>, indent: String = "  ") {
    map.forEach { (key, value) ->
        when (value) {
            null -> println("${indent}$key: null")
            is Map<*, *> -> {
                println("${indent}$key: Map (${value::class.qualifiedName})")
                @Suppress("UNCHECKED_CAST")
                displayMapTypes(value as Map<String, Any?>, "$indent  ")
            }
            else -> {
                val typeName = value::class.qualifiedName ?: value.javaClass.name
                println("${indent}$key: ${value::class.java.simpleName} ($typeName)")
            }
        }
    }
}