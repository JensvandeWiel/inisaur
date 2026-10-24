package serialization

import annotations.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

// Test data classes moved to top level (outside of inner classes)
@IniSection("TestSection")
data class SimpleConfig(
    val name: String,
    val count: Int,
    val isEnabled: Boolean
)

@IniSection("ServerSettings")
data class ServerConfig(
    val serverName: String,
    val maxPlayers: Int
)

@IniSection("GameplaySettings")
data class GameplayConfig(
    val difficulty: Float,
    val enablePvP: Boolean
)

@IniSerializable
data class GameConfig(
    @IniSection("ServerSettings") val server: ServerConfig,
    @IniSection("GameplaySettings") val gameplay: GameplayConfig
)

@IniSerializable
data class OptionalGameConfig(
    @IniSection val server: ServerConfig,
    @IniSection val gameplay: GameplayConfig?
)

@IniSection("CustomSection")
data class AnnotatedConfig(
    @IniProperty(name = "custom_name")
    val name: String,

    @IniBoolean(capitalized = false)
    val isEnabled: Boolean,

    @IniProperty(ignore = true)
    val secretValue: String
)

@IniSection("ArraySection")
data class ArrayConfig(
    @IniArray(arrayType = ArrayType.CommaSeparatedArray)
    val commaSeparated: List<String>,

    @IniArray(arrayType = ArrayType.RepeatedLineArray)
    val repeatedLines: List<String>
)

@IniSection("IntArraySection")
data class IntArrayConfig(
    @IniArray(arrayType = ArrayType.CommaSeparatedArray)
    val ids: List<Int>
)

@IniSection("StructArraySection")
data class StructArrayConfig(
    @IniArray(arrayType = ArrayType.RepeatedLineArray)
    val positions: List<Position>
)

@IniStruct
data class Position(val x: Int, val y: Int, val z: Int)

@IniStruct
data class QuotedStruct(
    @IniProperty("Class")
    @IniString(quoted = true)
    val clazz: String = "",
)

@IniSection("QuotedStructSection")
data class QuotedStructConfig(
    @IniProperty("Entry")
    val entry: QuotedStruct = QuotedStruct(),
)

@IniSection("EntitySection")
data class EntityConfig(
    val name: String,
    val position: Position
)

@IniSection("MapSection")
data class MapConfig(
    val indexedArray: Map<Int, String>,
    val namedMap: Map<String, String>
)

class UnannotatedClass(val name: String)

@IniSection("TestSection")
data class TestConfig(val name: String)

@IniSection("DefaultSection")
data class DefaultedConfig(
    val name: String = "Fallback",
    val enabled: Boolean
)

@IniSection("MixedDefaultSection")
data class MixedDefaultConfig(
    val title: String = "Default Title",
    val count: Int = 7,
    val enabled: Boolean = true
)

@IniStruct
data class Vector3(val x: Float, val y: Float, val z: Float)

@IniStruct
data class SpawnSettings(val minLevel: Int, val maxLevel: Int, val position: Vector3)

@IniStruct
data class Vector3WithDefaults(val x: Float = 1f, val y: Float = 2f, val z: Float = 3f)

@IniStruct
data class SpawnSettingsWithDefaults(
    val minLevel: Int = 5,
    val maxLevel: Int = 10,
    val position: Vector3WithDefaults = Vector3WithDefaults()
)

@IniSection("DefaultStructSection")
data class DefaultStructConfig(
    val settings: SpawnSettingsWithDefaults
)

@IniSection("PrimitiveDefaults")
data class PrimitiveDefaultsConfig(
    val title: String,
    val count: Int,
    val enabled: Boolean,
    val ratio: Float
)

@IniSection("CollectionDefaults")
data class CollectionDefaultsConfig(
    val tags: List<String>,
    val metadata: Map<String, String>
)

@IniSection("CollectionMapDefaults")
data class CollectionMapDefaultsConfig(
    val tags: List<String> = listOf("alpha", "beta"),
    val metadata: Map<String, String> = mapOf("hero" to "Player", "enemy" to "Boss")
)

@IniSection("NullableDefaults")
data class NullableDefaultsConfig(
    val title: String?,
    val tags: List<String>?,
    val metadata: Map<String, String>?
)

@IniSection("OptionalNullableDefaults")
data class OptionalNullableDefaultsConfig(
    val title: String? = "Optional Title",
    val tags: List<String>? = listOf("one", "two"),
    val metadata: Map<String, String>? = mapOf("mode" to "default")
)

@IniSection("DinoSpawner")
data class DinoConfig(
    val name: String,
    val enabled: Boolean,
    val settings: SpawnSettings,
    @IniArray val allowedBiomes: List<String>
)

@IniSection("ItemDrops")
data class ItemDrops(
    val dropTables: Map<String, Float>,
    @IniArray(arrayType = ArrayType.RepeatedLineArray) val specialItems: List<String>
)

@IniSerializable
data class FullGameConfig(
    val dinoConfig: DinoConfig,
    @IniSection("ItemDrops") val itemDrops: ItemDrops
)

class IniSerializerTest {

    @Nested
    inner class BasicSerializationTests {
        @Test
        fun `serialize simple section object`() {
            val config = SimpleConfig("Test", 42, true)
            val iniContent = IniSerializer.serializeSection(config)

            assertTrue(iniContent.contains("[TestSection]"))
            assertTrue(iniContent.contains("name=Test"))
            assertTrue(iniContent.contains("count=42"))
            assertTrue(iniContent.contains("isEnabled=True"))
        }

        @Test
        fun `deserialize uses constructor default when field is missing`() {
            val ini = """
                [DefaultSection]
                enabled=True
            """.trimIndent()

            val config = IniSerializer.deserializeSection<DefaultedConfig>(ini)

            assertEquals("Fallback", config.name)
            assertTrue(config.enabled)
        }

        @Test
        fun `deserialize uses constructor defaults for multiple missing fields`() {
            val ini = """
                [MixedDefaultSection]
            """.trimIndent()

            val config = IniSerializer.deserializeSection<MixedDefaultConfig>(ini)

            assertEquals("Default Title", config.title)
            assertEquals(7, config.count)
            assertTrue(config.enabled)
        }

        @Test
        fun `deserialize simple section object`() {
            val ini = """
                [TestSection]
                name=Test
                count=42
                isEnabled=True
            """.trimIndent()

            val config = IniSerializer.deserializeSection<SimpleConfig>(ini)

            assertEquals("Test", config.name)
            assertEquals(42, config.count)
            assertTrue(config.isEnabled)
        }
    }

    @Nested
    inner class MultiSectionTests {
        @Test
        fun `serialize multi-section object`() {
            val config = GameConfig(
                ServerConfig("My Server", 50),
                GameplayConfig(0.5f, true)
            )

            val iniContent = IniSerializer.serialize(config)

            assertTrue(iniContent.contains("[ServerSettings]"))
            assertTrue(iniContent.contains("serverName=My Server"))
            assertTrue(iniContent.contains("maxPlayers=50"))

            assertTrue(iniContent.contains("[GameplaySettings]"))
            assertTrue(iniContent.contains("difficulty=0.5"))
            assertTrue(iniContent.contains("enablePvP=True"))
        }

        @Test
        fun `deserialize multi-section object`() {
            val ini = """
                [ServerSettings]
                serverName=My Server
                maxPlayers=50
                
                [GameplaySettings]
                difficulty=0.5
                enablePvP=True
            """.trimIndent()

            val config = IniSerializer.deserialize<GameConfig>(ini)

            assertEquals("My Server", config.server.serverName)
            assertEquals(50, config.server.maxPlayers)
            assertEquals(0.5f, config.gameplay.difficulty)
            assertTrue(config.gameplay.enablePvP)
        }

        @Test
        fun `deserialize handles missing sections for optional properties`() {
            val ini = """
                [ServerSettings]
                serverName=My Server
                maxPlayers=50
            """.trimIndent()

            // This should fail because GameplaySettings is required
            assertFailsWith<IllegalArgumentException> {
                IniSerializer.deserialize<GameConfig>(ini)
            }
        }

        @Test
        fun `deserialize handles missing sections for nullable properties`() {
            val ini = """
                [ServerSettings]
                serverName=My Server
                maxPlayers=50
            """.trimIndent()

            // This should succeed because gameplay is nullable
            val config = IniSerializer.deserialize<OptionalGameConfig>(ini)

            assertEquals("My Server", config.server.serverName)
            assertEquals(50, config.server.maxPlayers)
            assertNull(config.gameplay)
        }
    }

    @Nested
    inner class PropertyAnnotationTests {
        @Test
        fun `property annotations work correctly`() {
            val config = AnnotatedConfig("Test", true, "secret")
            val iniContent = IniSerializer.serializeSection(config)

            assertTrue(iniContent.contains("custom_name=Test"))
            assertTrue(iniContent.contains("isEnabled=true")) // lowercase due to annotation
            assertFalse(iniContent.contains("secretValue")) // ignored property
        }
    }

    @Nested
    inner class ArrayTests {
        @Test
        fun `array annotations work correctly`() {
            val config = ArrayConfig(
                listOf("one", "two", "three"),
                listOf("first", "second", "third")
            )

            val iniContent = IniSerializer.serializeSection(config)

            assertTrue(iniContent.contains("commaSeparated=one,two,three"))

            // Repeated lines should appear multiple times
            val lines = iniContent.lines()
            assertTrue(lines.any { it == "repeatedLines=first" })
            assertTrue(lines.any { it == "repeatedLines=second" })
            assertTrue(lines.any { it == "repeatedLines=third" })
        }

        @Test
        fun `single comma-separated value deserializes as a one-item list`() {
            val config = IniSerializer.deserializeSection<ArrayConfig>(
                """
                [ArraySection]
                commaSeparated=one
                repeatedLines=first
                """.trimIndent()
            )

            assertEquals(listOf("one"), config.commaSeparated)
            assertEquals(listOf("first"), config.repeatedLines)
        }

        @Test
        fun `single integer value deserializes as a one-item list`() {
            val config = IniSerializer.deserializeSection<IntArrayConfig>(
                """
                [IntArraySection]
                ids=111
                """.trimIndent()
            )

            assertEquals(listOf(111), config.ids)
        }

        @Test
        fun `empty scalar value deserializes as an empty list`() {
            val config = IniSerializer.deserializeSection<ArrayConfig>(
                """
                [ArraySection]
                commaSeparated=
                repeatedLines=
                """.trimIndent()
            )

            assertTrue(config.commaSeparated.isEmpty())
            assertTrue(config.repeatedLines.isEmpty())
        }

        @Test
        fun `repeated struct lines deserialize to struct objects`() {
            val config = StructArrayConfig(
                positions = listOf(Position(1, 2, 3), Position(4, 5, 6))
            )

            val text = IniSerializer.serializeSection(config)

            assertTrue(text.contains("positions=(x=1,y=2,z=3)"))

            val parsed = IniSerializer.deserializeSection<StructArrayConfig>(text)
            assertEquals(config, parsed)
        }

        @Test
        fun `single repeated struct line deserializes as a one-item struct list`() {
            val text = """
                [StructArraySection]
                positions=(x=7,y=8,z=9)
            """.trimIndent()

            val parsed = IniSerializer.deserializeSection<StructArrayConfig>(text)

            assertEquals(listOf(Position(7, 8, 9)), parsed.positions)
        }
    }

    @Nested
    inner class StructTests {
        @Test
        fun `struct values are serialized correctly`() {
            val config = EntityConfig("Player", Position(10, 20, 30))
            val iniContent = IniSerializer.serializeSection(config)

            assertTrue(iniContent.contains("position=(x=10,y=20,z=30)"))
        }

        @Test
        fun `deserialize preserves nested struct defaults when some fields are missing`() {
            val ini = """
                [DefaultStructSection]
                settings=(position=(x=42.0))
            """.trimIndent()

            val config = IniSerializer.deserializeSection<DefaultStructConfig>(ini)

            assertEquals(5, config.settings.minLevel)
            assertEquals(10, config.settings.maxLevel)
            assertEquals(42f, config.settings.position.x)
            assertEquals(2f, config.settings.position.y)
            assertEquals(3f, config.settings.position.z)
        }

        @Test
        fun `deserialize uses nested struct defaults when struct field is missing`() {
            val ini = """
                [DefaultStructSection]
            """.trimIndent()

            val config = IniSerializer.deserializeSection<DefaultStructConfig>(ini)

            assertEquals(5, config.settings.minLevel)
            assertEquals(10, config.settings.maxLevel)
            assertEquals(1f, config.settings.position.x)
            assertEquals(2f, config.settings.position.y)
            assertEquals(3f, config.settings.position.z)
        }

        @Test
        fun `IniString forces quotes inside struct values`() {
            val text = IniSerializer.serializeSection(
                QuotedStructConfig(QuotedStruct(clazz = "")),
            )

            assertTrue(text.contains("Entry=(Class=\"\")"))
            assertEquals(QuotedStruct(""), IniSerializer.deserializeSection<QuotedStructConfig>(text).entry)

            val quotedText = IniSerializer.serializeSection(
                QuotedStructConfig(QuotedStruct(clazz = "Rex")),
            )

            assertTrue(quotedText.contains("Entry=(Class=\"Rex\")"))
        }
    }

    @Nested
    inner class DefaultValueTests {
        @Test
        fun `deserialize uses primitive default values when fields are missing`() {
            val ini = """
                [PrimitiveDefaults]
            """.trimIndent()

            val config = IniSerializer.deserializeSection<PrimitiveDefaultsConfig>(ini)

            assertEquals("", config.title)
            assertEquals(0, config.count)
            assertFalse(config.enabled)
            assertEquals(0f, config.ratio)
        }

        @Test
        fun `deserialize uses empty collection and map defaults when fields are missing`() {
            val ini = """
                [CollectionDefaults]
            """.trimIndent()

            val config = IniSerializer.deserializeSection<CollectionDefaultsConfig>(ini)

            assertEquals(emptyList<String>(), config.tags)
            assertEquals(emptyMap<String, String>(), config.metadata)
        }

        @Test
        fun `deserialize uses non-empty collection and map defaults when fields are missing`() {
            val ini = """
                [CollectionMapDefaults]
            """.trimIndent()

            val config = IniSerializer.deserializeSection<CollectionMapDefaultsConfig>(ini)

            assertEquals(listOf("alpha", "beta"), config.tags)
            assertEquals(mapOf("hero" to "Player", "enemy" to "Boss"), config.metadata)
        }

        @Test
        fun `deserialize keeps provided collection values while defaulting missing map values`() {
            val ini = """
                [CollectionMapDefaults]
                tags=one,two
            """.trimIndent()

            val config = IniSerializer.deserializeSection<CollectionMapDefaultsConfig>(ini)

            assertEquals(listOf("one", "two"), config.tags)
            assertEquals(mapOf("hero" to "Player", "enemy" to "Boss"), config.metadata)
        }

        @Test
        fun `deserialize converts explicit empty string to null for nullable property`() {
            val ini = """
                [NullableDefaults]
                title=
            """.trimIndent()

            val config = IniSerializer.deserializeSection<NullableDefaultsConfig>(ini)

            assertNull(config.title)
            assertNull(config.tags)
            assertNull(config.metadata)
        }

        @Test
        fun `deserialize returns null for nullable fields when fields are missing`() {
            val ini = """
                [NullableDefaults]
            """.trimIndent()

            val config = IniSerializer.deserializeSection<NullableDefaultsConfig>(ini)

            assertNull(config.title)
            assertNull(config.tags)
            assertNull(config.metadata)
        }

        @Test
        fun `deserialize uses optional defaults for nullable fields when fields are missing`() {
            val ini = """
                [OptionalNullableDefaults]
            """.trimIndent()

            val config = IniSerializer.deserializeSection<OptionalNullableDefaultsConfig>(ini)

            assertEquals("Optional Title", config.title)
            assertEquals(listOf("one", "two"), config.tags)
            assertEquals(mapOf("mode" to "default"), config.metadata)
        }

        @Test
        fun `deserialize uses optional defaults when nullable property is explicitly empty`() {
            val ini = """
                [OptionalNullableDefaults]
                title=
            """.trimIndent()

            val config = IniSerializer.deserializeSection<OptionalNullableDefaultsConfig>(ini)

            assertNull(config.title)
            assertEquals(listOf("one", "two"), config.tags)
            assertEquals(mapOf("mode" to "default"), config.metadata)
        }
    }

    @Nested
    inner class MapTests {
        @Test
        fun `map values are serialized correctly`() {
            val config = MapConfig(
                mapOf(0 to "zero", 1 to "one", 2 to "two"),
                mapOf("key1" to "value1", "key2" to "value2")
            )

            val iniContent = IniSerializer.serializeSection(config)
            val lines = iniContent.lines()

            // Indexed array format
            assertTrue(lines.any { it == "indexedArray[0]=zero" })
            assertTrue(lines.any { it == "indexedArray[1]=one" })
            assertTrue(lines.any { it == "indexedArray[2]=two" })

            // Named map format
            assertTrue(lines.any { it == "namedMap[key1]=value1" })
            assertTrue(lines.any { it == "namedMap[key2]=value2" })
        }
    }

    @Nested
    inner class ErrorHandlingTests {
        @Test
        fun `serializing unannotated class throws exception`() {
            val instance = UnannotatedClass("test")

            assertFailsWith<IllegalArgumentException> {
                IniSerializer.serializeSection(instance)
            }

            assertFailsWith<IllegalArgumentException> {
                IniSerializer.serialize(instance)
            }
        }

        @Test
        fun `deserializing to unannotated class throws exception`() {
            val ini = "[TestSection]\nname=test"

            assertFailsWith<IllegalArgumentException> {
                IniSerializer.deserializeSection<UnannotatedClass>(ini)
            }

            assertFailsWith<IllegalArgumentException> {
                IniSerializer.deserialize<UnannotatedClass>(ini)
            }
        }

        @Test
        fun `deserializing with missing section throws exception`() {
            val ini = "[WrongSection]\nname=test"

            assertFailsWith<IllegalArgumentException> {
                IniSerializer.deserializeSection<TestConfig>(ini)
            }
        }
    }

    @Nested
    inner class ComplexTests {
        @Test
        fun `complex nested config serializes and deserializes correctly`() {
            val config = FullGameConfig(
                DinoConfig(
                    "T-Rex",
                    true,
                    SpawnSettings(10, 150, Vector3(100f, 200f, 50f)),
                    listOf("Forest", "Mountain", "Volcano")
                ),
                ItemDrops(
                    mapOf("Common" to 0.7f, "Rare" to 0.2f, "Legendary" to 0.1f),
                    listOf("Saddle", "Weapon", "Armor")
                )
            )

            val iniContent = IniSerializer.serialize(config)
            println(iniContent)

            // Test a few key elements in the serialized output
            assertTrue(iniContent.contains("[DinoSpawner]"))
            assertTrue(iniContent.contains("name=T-Rex"))
            assertTrue(iniContent.contains("allowedBiomes=Forest,Mountain,Volcano"))
            assertTrue(iniContent.contains("settings=(maxLevel=150,minLevel=10,position=(x=100.0,y=200.0,z=50.0))"))

            assertTrue(iniContent.contains("[ItemDrops]"))
            assertTrue(iniContent.contains("dropTables[Common]=0.7"))
            assertTrue(iniContent.contains("dropTables[Rare]=0.2"))
            assertTrue(iniContent.contains("dropTables[Legendary]=0.1"))

            // Now test full round-trip
            val deserializedConfig = IniSerializer.deserialize<FullGameConfig>(iniContent)

            assertEquals("T-Rex", deserializedConfig.dinoConfig.name)
            assertTrue(deserializedConfig.dinoConfig.enabled)
            assertEquals(10, deserializedConfig.dinoConfig.settings.minLevel)
            assertEquals(150, deserializedConfig.dinoConfig.settings.maxLevel)
            assertEquals(100f, deserializedConfig.dinoConfig.settings.position.x)
            assertEquals(200f, deserializedConfig.dinoConfig.settings.position.y)
            assertEquals(50f, deserializedConfig.dinoConfig.settings.position.z)
            assertEquals(listOf("Forest", "Mountain", "Volcano"), deserializedConfig.dinoConfig.allowedBiomes)

            assertEquals(0.7f, deserializedConfig.itemDrops.dropTables["Common"])
            assertEquals(0.2f, deserializedConfig.itemDrops.dropTables["Rare"])
            assertEquals(0.1f, deserializedConfig.itemDrops.dropTables["Legendary"])
            assertTrue(deserializedConfig.itemDrops.specialItems.contains("Saddle"))
            assertTrue(deserializedConfig.itemDrops.specialItems.contains("Weapon"))
            assertTrue(deserializedConfig.itemDrops.specialItems.contains("Armor"))
        }
    }
}

