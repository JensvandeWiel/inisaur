/**
 * Defines the types of tokens that can be identified during INI file lexical analysis.
 *
 * These token types represent the syntactic elements found in INI files, such as section headers,
 * keys, values, and special characters.
 *
 * @property SECTION_HEADER Represents a section header (e.g., "[SectionName]")
 * @property KEY Represents a key in a key-value pair
 * @property EQUALS Represents an equals sign separating key and value
 * @property VALUE_STRING Represents a string value
 * @property VALUE_INTEGER Represents an integer value
 * @property VALUE_FLOAT Represents a floating-point value
 * @property VALUE_BOOLEAN Represents a boolean value (true/false)
 * @property NUMERIC_INDEX Represents an indexed key with numeric index (e.g., "key[0]")
 * @property NAMED_INDEX Represents an indexed key with named index (e.g., "key[name]")
 * @property STRUCT_START Represents the start of a struct value ("(")
 * @property STRUCT_END Represents the end of a struct value (")")
 * @property COMMA Represents a comma separating values in arrays or structs
 * @property NEWLINE Represents a line break
 * @property EOF Represents the end of the file
 */
enum class TokenType {
    SECTION_HEADER,
    KEY,
    EQUALS,
    VALUE_STRING,
    VALUE_INTEGER,
    VALUE_FLOAT,
    VALUE_BOOLEAN,
    NUMERIC_INDEX,
    NAMED_INDEX,
    STRUCT_START,
    STRUCT_END,
    COMMA,
    NEWLINE,
    EOF,
}

