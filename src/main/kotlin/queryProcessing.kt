import java.io.File

enum class DiagramType(val str: String) {
    Round("round"), Histogram("histogram"), ScatterPlot("scatterplot")
}

typealias Type = String
typealias Value = Float

data class Element(val type: Type, val value: Value)
typealias Data = List<Element>

data class Query(val diagramType: DiagramType, val data: Data, val file: File)

fun parseArgs(args: Array<String>): Query {
    if (args.isEmpty()) {
        throw EmptyQuery()
    }
    if (args.size <= 2) {
        throw NotEnoughArguments()
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
    if (args.size % 2 == 1) {
        throw InvalidArgumentsNumber()
    }
    val data = mutableListOf<Element>()
    for (i in 1 until args.size - 1 step 2) {
        val type: Type = args[i]
        val value: Float = args[i + 1].toFloatOrNull() ?: throw InvalidArgument(type, args[i + 1])
        if (diagramType == DiagramType.Round && value < 0) {
            throw NegativeArgument(diagramType, type, value)
        }
        data.add(Element(type, value))
    }

    val fileName = args[args.size - 1]
    val file = File(fileName)
    if (!file.exists()) {
        file.createNewFile()
    }
    return Query(diagramType, data, file)
}