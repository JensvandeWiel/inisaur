package exceptions

class ParseException(
    message: String,
    val line : Int,
    val column : Int,
) : Exception("$message at position ($line:$column)") {
    constructor(message: String) : this(message, -1, -1)
}
