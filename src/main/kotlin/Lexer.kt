class Lexer(private val input: String) {
    private var position: Int = 0
    private var line: Int = 1
    private var column: Int = 1
    private var lastTokenType: TokenType? = null

    private val currentChar: Char
        get() = if (position < input.length) input[position] else '\u0000'

    fun advance() {
        if (currentChar == '\n') {
            line++
            column = 1
        } else {
            column++
        }
        position++
    }

    fun peek(): Char {
        return if (position + 1 < input.length) input[position + 1] else '\u0000'
    }

    fun skipWhitespace() {
        while (currentChar.isWhitespace() && currentChar != '\n') {
            advance()
        }
    }

    fun skipComment() {
        while (currentChar != '\n' && currentChar != '\u0000') {
            advance()
        }
    }

    fun nextToken(): Token {
        while (currentChar != '\u0000') {
            val startLine = line
            val startCol = column

            // If last token was EQUALS, always read a value next
            if (lastTokenType == TokenType.EQUALS) {
                val token = when {
                    currentChar == '"' -> readQuotedString()
                    currentChar == '(' -> {
                        advance()
                        Token(TokenType.STRUCT_START, "(", startLine, startCol)
                    }
                    else -> readUnquotedValue()
                }
                lastTokenType = token.type
                return token
            }

            when {
                currentChar.isWhitespace() && currentChar != '\n' -> {
                    skipWhitespace()
                    continue
                }
                currentChar == ';' -> {
                    skipComment()
                    continue
                }
                currentChar == '\n' -> {
                    advance()
                    lastTokenType = TokenType.NEWLINE
                    return Token(TokenType.NEWLINE, "\\n", startLine, startCol)
                }
                currentChar == '[' -> {
                    val token = readSectionHeader()
                    lastTokenType = token.type
                    return token
                }
                currentChar == '=' -> {
                    advance()
                    lastTokenType = TokenType.EQUALS
                    return Token(TokenType.EQUALS, "=", startLine, startCol)
                }
                currentChar == '(' -> {
                    advance()
                    lastTokenType = TokenType.STRUCT_START
                    return Token(TokenType.STRUCT_START, "(", startLine, startCol)
                }
                currentChar == ')' -> {
                    advance()
                    lastTokenType = TokenType.STRUCT_END
                    return Token(TokenType.STRUCT_END, ")", startLine, startCol)
                }
                currentChar == ',' -> {
                    advance()
                    lastTokenType = TokenType.COMMA
                    return Token(TokenType.COMMA, ",", startLine, startCol)
                }
                currentChar == '"' -> {
                    val token = readQuotedString()
                    lastTokenType = token.type
                    return token
                }
                currentChar.isLetter() || currentChar == '_' -> {
                    val token = readKey()
                    lastTokenType = token.type
                    return token
                }
                currentChar == '-' || currentChar.isDigit() -> {
                    val token = readNumber()
                    lastTokenType = token.type
                    return token
                }
                else -> {
                    val token = readUnquotedValue()
                    lastTokenType = token.type
                    return token
                }
            }
        }
        lastTokenType = TokenType.EOF
        return Token(TokenType.EOF, null, line, column)
    }

    private fun readSectionHeader(): Token {
        val startLine = line
        val startCol = column
        advance() // consume [

        val sb = StringBuilder()
        while (currentChar != ']' && currentChar != '\n' && currentChar != '\u0000') {
            sb.append(currentChar)
            advance()
        }

        if (currentChar != ']') {
            throw IllegalArgumentException("Unterminated section header at $startLine:$startCol")
        }
        advance() // consume ]

        return Token(TokenType.SECTION_HEADER, sb.toString().trim(), startLine, startCol)
    }

    private fun readKey(): Token {
        val startLine = line
        val startCol = column
        val sb = StringBuilder()

        // Read the key name
        while (currentChar.isLetterOrDigit() || currentChar == '_') {
            sb.append(currentChar)
            advance()
        }

        // Check for array index notation [index]
        if (currentChar == '[') {
            sb.append(currentChar)
            advance() // consume [

            // Read the index (either numeric or named)
            val indexStart = position
            while (currentChar != ']' && currentChar != '\n' && currentChar != '\u0000') {
                sb.append(currentChar)
                advance()
            }

            if (currentChar == ']') {
                sb.append(currentChar)
                advance() // consume ]

                val key = sb.toString()
                val indexContent = key.substring(key.indexOf('[') + 1, key.lastIndexOf(']'))

                // Determine if this is a numeric index or named index
                return if (indexContent.toIntOrNull() != null) {
                    Token(TokenType.NUMERIC_INDEX, key, startLine, startCol)
                } else {
                    Token(TokenType.NAMED_INDEX, key, startLine, startCol)
                }
            }
        }

        return Token(TokenType.KEY, sb.toString(), startLine, startCol)
    }

    private fun readQuotedString(): Token {
        val startLine = line
        val startCol = column
        advance() // consume opening quote

        val sb = StringBuilder()
        while (currentChar != '"' && currentChar != '\u0000' && currentChar != '\n') {
            if (currentChar == '\\') {
                advance()
                when (currentChar) {
                    '\\' -> sb.append('\\')
                    '"'  -> sb.append('"')
                    'n'  -> sb.append('\n')
                    't'  -> sb.append('\t')
                    else -> sb.append(currentChar)
                }
            } else {
                sb.append(currentChar)
            }
            advance()
        }

        if (currentChar != '"') {
            throw IllegalArgumentException("Unterminated quoted string at $startLine:$startCol")
        }
        advance() // consume closing quote

        return Token(TokenType.VALUE_STRING, sb.toString(), startLine, startCol)
    }

    private fun readUnquotedValue(): Token {
        val startLine = line
        val startCol = column
        val sb = StringBuilder()

        // Read until newline, end of input, structural character or a comment character
        while (currentChar != '\n' && currentChar != '\u0000'
            && currentChar != ',' && currentChar != ')' && currentChar != '(' && currentChar != ';') {
            sb.append(currentChar)
            advance()
        }

        val raw = sb.toString().trim()

        return when {
            raw.equals("true", ignoreCase = true) || raw.equals("false", ignoreCase = true) ->
                Token(TokenType.VALUE_BOOLEAN, raw, startLine, startCol)
            raw.toIntOrNull() != null ->
                Token(TokenType.VALUE_INTEGER, raw, startLine, startCol)
            raw.toFloatOrNull() != null ->
                Token(TokenType.VALUE_FLOAT, raw, startLine, startCol)
            else ->
                Token(TokenType.VALUE_STRING, raw, startLine, startCol)
        }
    }

    private fun readNumber(): Token {
        val startLine = line
        val startCol = column
        val sb = StringBuilder()

        if (currentChar == '-') {
            sb.append(currentChar)
            advance()
        }

        while (currentChar.isDigit()) {
            sb.append(currentChar)
            advance()
        }

        var isFloat = false
        if (currentChar == '.') {
            isFloat = true
            sb.append('.')
            advance()
            while (currentChar.isDigit()) {
                sb.append(currentChar)
                advance()
            }
        }

        val value = sb.toString()
        return if (isFloat)
            Token(TokenType.VALUE_FLOAT, value, startLine, startCol)
        else
            Token(TokenType.VALUE_INTEGER, value, startLine, startCol)
    }
}
