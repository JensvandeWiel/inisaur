data class IniFile(val sections: List<Section>) {
    override fun toString(): String {
        return sections.joinToString("\n\n")
    }
}
