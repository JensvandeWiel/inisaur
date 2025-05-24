package parser

import exceptions.ParseException
import kotlin.jvm.Throws

class Parser(private val lexer: Lexer) {
    private var currentToken: Token = lexer.nextToken()

    private fun eat(type: TokenType) {
        if (currentToken.type == type) {
            currentToken = lexer.nextToken()
        } else {
            throw IllegalArgumentException("Expected $type but got ${currentToken.type} at ${currentToken.line}:${currentToken.column}")
        }
    }

    @Throws(ParseException::class)
    fun parse(): IniFile {
        val sections = mutableListOf<Section>()
        while (currentToken.type != TokenType.EOF) {
            if (currentToken.type == TokenType.SECTION_HEADER) {
                sections.add(parseSection())
            } else {
                eat(currentToken.type) // skip unexpected tokens
            }
        }
        return IniFile(sections)
    }

    private fun parseSection(): Section {
        val name = currentToken.value
        if (name == null || name.isBlank()) {
            throw IllegalArgumentException("Section header is missing a name at ${currentToken.line}:${currentToken.column}")
        }
        eat(TokenType.SECTION_HEADER)

        // Collect all entries first
        val rawEntries = mutableListOf<Entry>()
        while (currentToken.type != TokenType.SECTION_HEADER && currentToken.type != TokenType.EOF) {
            if (currentToken.type == TokenType.NEWLINE) {
                eat(TokenType.NEWLINE)
                continue
            }
            parseEntry()?.let { rawEntries.add(it) }
        }

        // Now consolidate entries with the same key
        val entriesMap = mutableMapOf<String, Entry>()

        for (entry in rawEntries) {
            val key = entry.key

            when (entry) {
                is IndexedArray -> {
                    // Merge IndexedArrays with same key
                    if (entriesMap.containsKey(key) && entriesMap[key] is IndexedArray) {
                        val existingEntry = entriesMap[key] as IndexedArray
                        val combinedValues = existingEntry.indexedValues.toMutableMap()
                        combinedValues.putAll(entry.indexedValues)
                        entriesMap[key] = IndexedArray(key, combinedValues)
                    } else {
                        entriesMap[key] = entry
                    }
                }
                is MapEntry -> {
                    // Merge MapEntries with same key
                    if (entriesMap.containsKey(key) && entriesMap[key] is MapEntry) {
                        val existingEntry = entriesMap[key] as MapEntry
                        val combinedMap = existingEntry.value.toMutableMap()
                        combinedMap.putAll(entry.value)
                        entriesMap[key] = MapEntry(key, combinedMap)
                    } else {
                        entriesMap[key] = entry
                    }
                }
                is Plain -> {
                    // Create or update RepeatedLineArray for same keys
                    if (entriesMap.containsKey(key)) {
                        val existingEntry = entriesMap[key]
                        when (existingEntry) {
                            is Plain -> {
                                // Convert two Plain entries into a RepeatedLineArray
                                val values = mutableListOf(existingEntry.value, entry.value)
                                entriesMap[key] = RepeatedLineArray(key, values)
                            }
                            is RepeatedLineArray -> {
                                // Add to existing RepeatedLineArray
                                val newValues = existingEntry.values.toMutableList()
                                newValues.add(entry.value)
                                entriesMap[key] = RepeatedLineArray(key, newValues)
                            }
                            else -> {
                                // Replace with the new entry
                                entriesMap[key] = entry
                            }
                        }
                    } else {
                        entriesMap[key] = entry
                    }
                }
                is CommaSeparatedArray -> {
                    // For CommaSeparatedArray, just keep the last one or merge if needed
                    entriesMap[key] = entry
                }
                is RepeatedLineArray -> {
                    // Merge with existing RepeatedLineArray if it exists
                    if (entriesMap.containsKey(key) && entriesMap[key] is RepeatedLineArray) {
                        val existingEntry = entriesMap[key] as RepeatedLineArray
                        val combinedValues = existingEntry.values.toMutableList()
                        combinedValues.addAll(entry.values)
                        entriesMap[key] = RepeatedLineArray(key, combinedValues)
                    } else {
                        entriesMap[key] = entry
                    }
                }
            }
        }

        return Section(name, entriesMap.values.toList())
    }

    private fun parseEntry(): Entry? {
        // Key or Key[0] or Key[Name]
        val keyToken = currentToken
        if (keyToken.type != TokenType.KEY && keyToken.type != TokenType.NUMERIC_INDEX
            && keyToken.type != TokenType.NAMED_INDEX && keyToken.type != TokenType.ARRAY_INDEX) {
            while (currentToken.type != TokenType.NEWLINE && currentToken.type != TokenType.EOF) eat(currentToken.type)
            if (currentToken.type == TokenType.NEWLINE) eat(TokenType.NEWLINE)
            return null
        }

        val rawKey = keyToken.value ?: ""
        val isNumericIndexed = keyToken.type == TokenType.NUMERIC_INDEX
        val isNamedIndexed = keyToken.type == TokenType.NAMED_INDEX
        // Legacy support for ARRAY_INDEX
        val isLegacyIndexed = keyToken.type == TokenType.ARRAY_INDEX

        // Extract base key and index if needed
        val key: String
        var index: String? = null

        if (isNumericIndexed || isNamedIndexed || isLegacyIndexed) {
            val indexStart = rawKey.indexOf('[')
            val indexEnd = rawKey.lastIndexOf(']')

            if (indexStart > 0 && indexEnd > indexStart) {
                key = rawKey.substring(0, indexStart)
                index = rawKey.substring(indexStart + 1, indexEnd)
            } else {
                key = rawKey
            }
        } else {
            key = rawKey
        }

        eat(keyToken.type)
        eat(TokenType.EQUALS)

        // writer.Struct
        if (currentToken.type == TokenType.STRUCT_START) {
            val struct = parseStruct()
            skipLine()
            return Plain(key, struct)
        }

        // Value(s)
        val values = mutableListOf<Value>()
        values.add(parseValue())
        while (currentToken.type == TokenType.COMMA) {
            eat(TokenType.COMMA)
            values.add(parseValue())
        }

        // End of line
        skipLine()

        // Determine entry type based on token type and index
        return when {
            isNamedIndexed && index != null -> {
                // Key[Name]=Value
                MapEntry(key, mapOf(index to values.first()))
            }
            isNumericIndexed && index != null -> {
                // Key[0]=Value
                val indexNum = index.toInt()
                IndexedArray(key, mapOf(indexNum to values.first()))
            }
            isLegacyIndexed && index != null -> {
                // Handle legacy ARRAY_INDEX based on whether index is numeric
                if (index.toIntOrNull() != null) {
                    val indexNum = index.toInt()
                    IndexedArray(key, mapOf(indexNum to values.first()))
                } else {
                    MapEntry(key, mapOf(index to values.first()))
                }
            }
            values.size > 1 -> {
                // Key=Val1,Val2
                CommaSeparatedArray(key, values)
            }
            else -> {
                // Plain key=value
                Plain(key, values.first())
            }
        }
    }

    private fun parseValue(): Value {
        return when (currentToken.type) {
            TokenType.VALUE_STRING -> {
                val value = currentToken.value ?: ""
                eat(TokenType.VALUE_STRING)
                StringValue(value)
            }
            TokenType.VALUE_INTEGER -> {
                val value = currentToken.value ?: "0"
                eat(TokenType.VALUE_INTEGER)
                IntValue(value.toInt())
            }
            TokenType.VALUE_FLOAT -> {
                val value = currentToken.value ?: "0.0"
                eat(TokenType.VALUE_FLOAT)
                FloatValue(value.toFloat())
            }
            TokenType.VALUE_BOOLEAN -> {
                val value = currentToken.value ?: "false"
                eat(TokenType.VALUE_BOOLEAN)
                BoolValue(value.equals("true", ignoreCase = true), value[0].isUpperCase())
            }
            TokenType.STRUCT_START -> parseStruct()
            else -> {
                val value = currentToken.value ?: ""
                eat(currentToken.type)
                StringValue(value)
            }
        }
    }

    private fun parseStruct(): StructValue {
        eat(TokenType.STRUCT_START)
        val fields = mutableMapOf<String, Any?>()

        while (currentToken.type != TokenType.STRUCT_END && currentToken.type != TokenType.EOF) {
            // Parse field name
            if (currentToken.type == TokenType.KEY) {
                val key = currentToken.value ?: ""
                eat(TokenType.KEY)
                eat(TokenType.EQUALS)

                // Parse field value
                val value = parseValue()
                fields[key] = valueToNativeType(value)

                // Eat comma if present
                if (currentToken.type == TokenType.COMMA) {
                    eat(TokenType.COMMA)
                }
            } else {
                // Skip unexpected tokens
                eat(currentToken.type)
            }
        }

        eat(TokenType.STRUCT_END)
        return StructValue(fields)
    }

    private fun valueToNativeType(value: Value): Any? {
        return when (value) {
            is StringValue -> value.value
            is IntValue -> value.value
            is FloatValue -> value.value
            is BoolValue -> value.value
            is StructValue -> value.fields
        }
    }

    private fun skipLine() {
        // Skip any content until end of line or EOF
        while (currentToken.type != TokenType.NEWLINE && currentToken.type != TokenType.EOF) {
            eat(currentToken.type)
        }

        // Eat the newline if present
        if (currentToken.type == TokenType.NEWLINE) {
            eat(TokenType.NEWLINE)
        }
    }
}
