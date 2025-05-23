package parser

data class Token(
    val type: TokenType,
    val value: String?,
    val line: Int,
    val column: Int
) {
    override fun toString(): String {
        return "Token(type=$type, value=`$value`, line=$line, column=$column)"
    }
}