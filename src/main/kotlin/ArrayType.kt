/**
 * Represents the different formats for storing array values in INI files.
 *
 * @property CommaSeparatedArray Arrays stored as comma-separated values on a single line (e.g., `key=value1,value2,value3`)
 * @property RepeatedLineArray Arrays stored as repeated keys across multiple lines (e.g., `key=value1\nkey=value2`)
 */
enum class ArrayType {
    CommaSeparatedArray,
    RepeatedLineArray,
}

