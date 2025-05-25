# Module Inisaur

A powerful Kotlin library for parsing, manipulating, and generating INI configuration files, with special support for ARK: Survival Evolved/Ascended configuration formats.

[![Kotlin](https://img.shields.io/badge/kotlin-1.8.0-blue.svg)](http://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Features

- 🔍 **Comprehensive Parsing** - Handles standard INI files plus custom ARK/Unreal Engine formats
- 🔄 **Full Type Support** - Native handling of strings, integers, floats, booleans, arrays, and nested structs
- 🧵 **Thread Safety** - Synchronous and asynchronous (suspending) API for concurrent access
- 📝 **Type-Safe Serialization** - Kotlin class annotations for easy serialization/deserialization
- 🛠️ **Rich API** - Intuitive methods for reading, writing, and manipulating INI data
- 🗂️ **Multi-Section Support** - Serialize and deserialize complete INI files with multiple sections

## Installation

### Gradle

```kotlin
repositories {
    mavenCentral()
    // Add your repository if published
}

dependencies {
    implementation("eu.wynq:inisaur:<version>")
}
```

### Maven

```xml
<dependency>
    <groupId>eu.wynq</groupId>
    <artifactId>inisaur</artifactId>
    <version><!--<version>--></version>
</dependency>
```

## Quick Start

### Parse an INI File

```kotlin
// Parse from string
val iniString = """
    [ServerSettings]
    ServerName=My ARK Server
    MaxPlayers=70
    EnablePvP=True
    
    [GameSettings]
    Difficulty=1.0
    AllowCaveFlyers=True
"""

val lexer = Lexer(iniString)
val parser = Parser(lexer)
val iniFile = parser.parse()

// Access sections and values
val serverName = iniFile.getStringValue("ServerSettings", "ServerName")
val maxPlayers = iniFile.getIntValue("ServerSettings", "MaxPlayers")
val enablePvP = iniFile.getBooleanValue("ServerSettings", "EnablePvP")

// Modify values
iniFile.setValue("ServerSettings", "ServerName", "My Awesome ARK Server")
iniFile.setValue("GameSettings", "Difficulty", 0.8f)

// Convert back to string
val outputString = iniFile.toString()
```

### Using Annotations for Section Serialization

```kotlin
// Define your section class
@IniSection("ServerSettings")
data class ServerConfig(
    val serverName: String,
    
    val maxPlayers: Int,
    
    @IniBoolean(capitalized = true)
    val enablePvP: Boolean,
    
    @IniArray(arrayType = ArrayType.CommaSeparatedArray)
    val enabledMods: List<String>,
    
    @IniProperty(name = "server_password")
    val password: String,
    
    @IniProperty(ignore = true)
    val internalValue: String
)

// Serialize a section to INI
val config = ServerConfig(
    serverName = "My ARK Server",
    maxPlayers = 70,
    enablePvP = true,
    enabledMods = listOf("12345", "67890"),
    password = "secret",
    internalValue = "ignored"
)

val iniString = IniSerializer.serializeSection(config)

// Deserialize from INI
val parsedConfig = IniSerializer.deserializeSection<ServerConfig>(iniString)
```

### Using Annotations for Multi-Section File Serialization

```kotlin
// Define your section classes
@IniSection("ServerSettings")
data class ServerConfig(
    val serverName: String,
    val maxPlayers: Int,
    val enablePvP: Boolean
)

@IniSection("GameSettings")
data class GameConfig(
    val difficulty: Float,
    val allowCaveFlyers: Boolean
)

// Define a class representing the full INI file
@IniSerializable
data class ArktConfig(
    @IniSection val server: ServerConfig,  // Use section name from the class
    @IniSection("CustomGameSettings") val game: GameConfig  // Override section name
)

// Create and populate an instance
val config = ArktConfig(
    server = ServerConfig("My Server", 70, true),
    game = GameConfig(0.8f, true)
)

// Serialize the full config to INI string
val iniString = IniSerializer.serialize(config)

// Deserialize from INI string
val parsedConfig = IniSerializer.deserialize<ArktConfig>(iniString)
```

### Async API

```kotlin
suspend fun configureServer() {
    val iniFile = IniFile()
    
    // All operations have async versions
    iniFile.addSectionAsync("ServerSettings")
    iniFile.setValueAsync("ServerSettings", "ServerName", "Async Server")
    iniFile.setValueAsync("ServerSettings", "MaxPlayers", 100)
    
    // Access values asynchronously
    val serverName = iniFile.getStringValueAsync("ServerSettings", "ServerName")
}
```

## Supported INI Features

- **Sections** - Standard `[Section]` format
- **Basic Types** - Strings, integers, floats, and booleans
- **Arrays** - Three formats:
  - Comma-separated: `key=val1,val2,val3`
  - Repeated lines: `key=val1` on multiple lines
  - Indexed: `key[0]=val1`, `key[1]=val2`
- **Maps** - Named indexed format: `key[name1]=val1`, `key[name2]=val2`
- **Structs** - Nested key-value pairs: `key=(subkey1=val1,subkey2=val2)`
- **Comments** - Lines starting with semicolons `;`

## Annotations

Inisaur provides annotations to configure how classes and properties are serialized:

- `@IniSerializable` - Marks a class as a complete INI file with multiple sections
- `@IniSection` - Marks a class as an INI section or a property as containing an INI section
- `@IniProperty` - Customizes property serialization (rename or ignore)
- `@IniBoolean` - Configures boolean formatting (capitalized or lowercase)
- `@IniArray` - Specifies array format (comma-separated or repeated lines)
- `@IniStruct` - Marks a class as a struct value for nested objects

## Documentation

Complete API documentation is available via Dokka.

For detailed information about the INI format supported by this library, refer to [spec.md](spec.md).

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
