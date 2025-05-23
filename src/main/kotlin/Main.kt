import parser.Lexer
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
        WelcomeMessage="Welcome to the island!"

        [Mods]
        ModList=123456,654321
        ModMap[Main]=Valguero
        ModMap[Event]=Ragnarok
        OverrideNamedEngramEntries=(EngramClassName="EngramEntry_Dino_Aid_X_C",EngramHidden=True,EngramPointsCost=75,EngramLevelRequirement=150,RemoveEngramPreReq=False)
        NestedStruct=(StructName="NestedStruct", StructValue=(Key1=Value1, Key2=Value2))
    """.trimIndent()

    val lexer = Lexer(input)

    while (true) {
        val token = lexer.nextToken()
        println(token)
        if (token.type == TokenType.EOF) break
    }
}