import parser.Lexer
import parser.Parser
import parser.TokenType

fun main() {
    val input = """
; This is a comment
[ServerSettings]
ServerName=ARK Server
MaxPlayers=70
EnablePvP=True
DifficultyOffset=0.2
CustomRecipeCostMultiplier=1.5
WelcomeMessage=Welcome to the khjghjghjgjkh
[Mods]
ModList=123456,654321
Ke1y=Value
Ke1y=Value2
Key[1]=32
Key[0]=16
Key[2]=64
ModMap[Main]=Valguero
ModMap[Event]=Ragnarok
OverrideNamedEngramEntries=(EngramClassName="EngramEntry_Dino_Aid_X_C",EngramHidden=True)
NestedStruct=(StructName="NestedStruct", StructValue=(Key1=Value1, Key2=Value2))
    """.trimIndent()

    val lexer = Lexer(input)

    while (lexer.nextToken().type != TokenType.EOF) {
        val token = lexer.nextToken()
        println("Token: ${token.type}, Value: ${token.value}, Line: ${token.line}, Column: ${token.column}")
    }

    val parser = Parser(Lexer(input))
    val tokens = parser.parse()
    for (section in tokens.sections) {
        println("Section: ${section.name}")
        for (entry in section.entries) {
            println(entry)
        }
    }
}