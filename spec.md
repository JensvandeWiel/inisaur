This document defines the configuration file format used to configure **ARK: Survival Evolved** and **ARK: Survival Ascended** servers and clients.

ARK’s INI format is a customized version of the [Unreal Engine INI specification](https://dev.epicgames.com/documentation/en-us/unreal-engine/configuration-files-in-unreal-engine#syntax), with added support for structs and (named) indexed arrays.

> Note: This specification only includes the features relevant to ARK. Some Unreal Engine configuration features are not used in practice and are therefore excluded.

---
## 1. Syntax

### 1.1 File Structure
INI files are structured into sections, each containing key-value pairs:
```ini
[Section1]
Key1=Value1
Key2=Value2

[Section2]
Key3=Value3
```

- Every key-value pair must belong to a `[Section]`.
- Each pair must be on a single line.
- The value may be empty:
```ini
[Section]
LogTemp=
```
### 1.2 Section Names
- Section names are case-sensitive alphabetic strings.
- You can use any name, but it must not include special characters such as `=`, `[`, or `]`.
### 1.3 Key Names
Keys are case-sensitive and may contain the following characters:

| Allowed Characters |
| ------------------ |
| A–Z (uppercase)    |
| a–z (lowercase)    |
| 0–9 (digits)       |
| _ (underscore)     |
Keys may also include array brackets `[]` at the end, with either an index or a named index:
- `Key[0]`
- `Key[SomeName]`
### 1.4 Value Types
Unlike standard INI files, ARK supports a variety of value types.
#### 1.4.1 Booleans
- Can be `True` / `False` or `true` / `false`, depending on the key.
- You must not mix the cases; follow the required format for each specific key.
#### 1.4.2 Integers
- Whole numbers such as `42` or `-42`.
#### 1.4.3 Floats
- Decimal numbers such as `1.5`.
#### 1.4.4 Strings
- Plain text like `Text`.
- Text with spaces must be enclosed in quotes: `"Spaced Text"`.
- Escape special characters inside quoted strings using a backslash `\`.
#### 1.4.5 Arrays
ARK INI supports different ways to define arrays.
##### 1.4.5.1 Comma-Delimited Arrays
```ini
Key=Val1,Val2,Val3
```
##### 1.4.5.2 Repeated Line Arrays
In this case the index is implicit and the order of the lines matters:
```ini
Key=Val1
Key=Val2
Key=Val3
```
##### 1.4.5.3 Indexed Arrays
```ini
Key[0]=Val1
Key[1]=Val2
Key[2]=Val3
```
##### 1.4.5.4 Named Indexed Arrays (Map)
```ini
Key[Key1]=Val1
Key[Key2]=Val2
Key[Key3]=Val3
```

#### 1.4.6 Structs
A struct is a value composed of nested key-value pairs:
```ini
Key=(SubKey1=Val1,SubKey2=Val2)
```
- Sub-keys inside a struct can use any supported type described in section 1.4.
- Structs can be nested
- Ensure the syntax is exact; malformed structs can prevent the config from loading.
### 1.5 Comments
- Comments start with `;`.
- Inline comments are not allowed.
  Examples:
```ini
; This is a valid comment
Key=True  ; ❌ Invalid — inline comments are not supported
```
### 1.6 Notes
#### 1.6.1 Validation
- Invalid keys are silently ignored.
- Incorrect value types may cause unpredictable behavior.
- Syntax errors—especially in nested structures—can prevent the configuration from being loaded entirely.
## 2. Sources

This specification is derived from both the Unreal Engine documentation and community documentation for ARK.
- [Unreal Engine Configuration File Syntax](https://dev.epicgames.com/documentation/en-us/unreal-engine/configuration-files-in-unreal-engine#syntax)
- [ARK Wiki – Server Configuration](https://ark.wiki.gg/wiki/Server_configuration)