data class Query(val diagramType: DiagramType, val data: Data)

fun parseArgs(args: Array<String>): Query {
    if (args.isEmpty()) {
        throw EmptyQuery()
    }
    val diagramName = args[0]
    var diagramType: DiagramType? = null
    for (type in DiagramType.values()) {
        if (diagramName == type.str) {
            diagramType = type
        }
    }
    if (diagramType == null) {
        throw UnsupportedDiagram(diagramName)
    }
    if (args.size == 1) {
        throw NoArguments()
    }
    if (args.size % 2 == 0) {
        throw InvalidArgumentsNumber()
    }
    val data = mutableListOf<Element>()
    for (i in 1 until args.size step 2) {
        val type: Type = args[i]
        val value: Float = args[i + 1].toFloatOrNull() ?: throw InvalidArgument(type, args[i + 1])
        if (diagramType == DiagramType.Round && value < 0) {
            throw NegativeArgument(diagramType, type, value)
        }
        data.add(Element(type, value))
    }
    return Query(diagramType, data)
}
