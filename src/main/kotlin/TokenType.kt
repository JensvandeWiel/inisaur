enum class TokenType {
    SECTION_HEADER,
    KEY,
    EQUALS,
    VALUE_STRING,
    VALUE_INTEGER,
    VALUE_FLOAT,
    VALUE_BOOLEAN,
    ARRAY_INDEX,     // Keeping for backward compatibility
    NUMERIC_INDEX,   // For array indices like Key[0]
    NAMED_INDEX,     // For map entries like Key[Name]
    STRUCT_START,
    STRUCT_END,
    COMMA,
    COMMENT,
    NEWLINE,
    EOF,
}

