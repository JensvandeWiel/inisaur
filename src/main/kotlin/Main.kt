import annotations.IniArray
import annotations.IniBoolean
import annotations.IniProperty
import annotations.IniSerializable
import annotations.IniStruct
import serialization.IniSerializer

@IniStruct
data class Location(
    val x: Float,
    val y: Float,
    val z: Float,
    val otherLocation : Location? = null
)

// Define a serializable class
@IniSerializable(sectionName = "GameSettings")
data class GameSettings(
    val playerName: String,

    @IniBoolean(capitalized = true)
    val enableTutorials: Boolean,

    @IniProperty(name = "Volume")
    val soundVolume: Float? = null,

    @IniArray(arrayType = ArrayType.CommaSeparatedArray)
    val enabledFeatures: List<String>,

    @IniArray(arrayType = ArrayType.RepeatedLineArray)
    val recentMaps: List<String>,

    val spawnLocation: Location,

    val checkpoints: Map<Int, String?>,

    val map: Map<String, Any?> = mapOf("key1" to null, "key2" to "value2"),

    @IniArray(arrayType = ArrayType.CommaSeparatedArray)
    val array: List<Any?> = listOf(1, 2, 3, "a", "b", "c"),

    @IniProperty(name = "TemporaryData")
    val temporaryData: String? = null
)

fun main() {
    // Example usage of the GameSettings class
    val settings = GameSettings(
        playerName = "Player1",
        enableTutorials = true,
        enabledFeatures = listOf("Feature1", "Feature2"),
        recentMaps = listOf("Map1", "Map2"),
        spawnLocation = Location(
            100.0f, 200.0f, 300.0f,
            otherLocation = Location(50.0f, 75.0f, 125.0f)
        ),
        checkpoints = mapOf(1 to "Checkpoint1", 2 to null)
    )

    // Here you would typically serialize `settings` to an INI format
    println(settings)

    val iniString = IniSerializer.serialize(settings)
    println("Serialized INI:\n$iniString")

    // And then deserialize it back to a GameSettings object
    val deserializedSettings = IniSerializer.deserialize<GameSettings>(iniString)
    println("Deserialized GameSettings:\n$deserializedSettings")

}