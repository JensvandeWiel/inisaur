/**
 * Represents a token in the INI file parsing process.
 *
 * Each token has a type, an optional value, and position information (line and column)
 * within the source file. Tokens are produced by the [Lexer] and consumed by the [Parser].
 *
 * @property type The type of token, as defined in [TokenType].
 * @property value The string value of the token, if applicable (may be null).
 * @property line The 1-based line number where this token appears in the source.
 * @property column The 1-based column number where this token appears in the source.
 */
data class Token(
    val type: TokenType,
    val value: String?,
    val line: Int,
    val column: Int
)

