# INI File Format Specification

Inisaur is designed to work with a customized version of the INI format that extends the [standard INI specification](https://en.wikipedia.org/wiki/INI_file) with additional data structures commonly used in game configuration files, particularly those in Unreal Engine-based games like **ARK: Survival Evolved** and **ARK: Survival Ascended**.

This specification documents the syntax and features supported by the Inisaur library.

## 1. Basic Structure

### 1.1 Sections and Key-Value Pairs

INI files are organized into sections, with each section containing key-value pairs:

```ini
[SectionName]
Key1=Value1
Key2=Value2

[AnotherSection]
Key3=Value3
```

Rules:
- Every key-value pair must belong to a section
- Each pair must appear on its own line
- Values may be empty: `Key=`
- Section names and keys are case-sensitive

### 1.2 Comments

Comments start with a semicolon (`;`) and continue to the end of the line:

```ini
; This is a comment
[Section]
; Another comment
Key=Value
```

Note: Inline comments (comments on the same line as a key-value pair) are not supported.

## 2. Data Types

### 2.1 Basic Types

#### 2.1.1 Strings

```ini
PlainString=Value
QuotedString="String with spaces or special characters"
EscapedQuotes="String with \"quotes\" inside"
```

Strings with spaces or special characters should be enclosed in double quotes. Within quoted strings, you can escape characters using a backslash (`\`).

#### 2.1.2 Numbers

- **Integers**: `Count=42`
- **Negative integers**: `Offset=-10`
- **Floating-point**: `Scale=1.5`

#### 2.1.3 Booleans

Boolean values can be represented in two forms:
- Capitalized: `EnableFeature=True` or `EnableFeature=False`
- Lowercase: `enableFeature=true` or `enableFeature=false`

The capitalization style should be consistent within your application.

### 2.2 Complex Types

#### 2.2.1 Arrays

Inisaur supports three array formats:

1. **Comma-separated arrays**:
   ```ini
   Colors=Red,Green,Blue
   Numbers=1,2,3,4,5
   ```

2. **Repeated key arrays**:
   ```ini
   Item=Sword
   Item=Shield
   Item=Potion
   ```

3. **Indexed arrays**:
   ```ini
   Inventory[0]=Sword
   Inventory[1]=Shield
   Inventory[2]=Potion
   ```

#### 2.2.2 Maps (Named Index Arrays)

Maps are represented using keys with named indices:

```ini
PlayerData[Alice]=100
PlayerData[Bob]=85
PlayerData[Charlie]=92
```

#### 2.2.3 Structs

Structs allow nesting key-value pairs within a single value:

```ini
PlayerCharacter=(Name="Alice",Health=100,Position=(X=0,Y=0,Z=0))
```

Structs:
- Are enclosed in parentheses
- Contain key-value pairs separated by commas
- Can be nested (structs within structs)
- Can contain any valid value type

## 3. Usage with Inisaur

The Inisaur library provides a type-safe way to work with this INI format through its API and annotation system.

### 3.1 Annotations

- `@IniSerializable` - Marks a class for INI serialization
- `@IniProperty` - Customizes property serialization behavior
- `@IniBoolean` - Configures boolean value representation
- `@IniArray` - Configures array representation format
- `@IniStruct` - Marks a class as a struct value

### 3.2 Value Types

Inisaur maps INI values to Kotlin types:
- `StringValue` - Represents string values
- `IntValue` - Represents integer values
- `FloatValue` - Represents floating-point values
- `BoolValue` - Represents boolean values
- `StructValue` - Represents structured values

### 3.3 Entry Types

Different ways values can be stored:
- `Plain` - Simple key-value pairs
- `CommaSeparatedArray` - Arrays as comma-separated values
- `RepeatedLineArray` - Arrays as repeated keys
- `IndexedArray` - Arrays with numeric indices
- `MapEntry` - Maps with string keys

## 4. Best Practices

1. **Be consistent** with your naming conventions and value formats
2. **Organize related settings** into logical sections
3. **Use appropriate data types** for each value
4. **Maintain compatibility** with existing parsers when working with game configuration files
5. **Validate input** when reading or writing values
