import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.WindowConstants

enum class DiagramType(val str: String) {
    Round("round"), Histogram("histogram"), ScatterPlot("scatterplot")
}

typealias Type = String
typealias Value = Double
data class Element(val type: Type, val value: Value)

typealias Data = List<Element>
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
        val value: Double = args[i + 1].toDoubleOrNull() ?: throw InvalidArgument(type, args[i + 1])
        data.add(Element(type, value))
    }
    return Query(diagramType, data)
}


fun main(args: Array<String>) {

    val query = parseArgs(args)
    println(query.diagramType)
    println(query.data)

    createWindow("pf-2021-viz")
}

fun createWindow(title: String) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = Renderer(window.layer)
    window.layer.addMouseMotionListener(MyMouseMotionAdapter)

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100, 100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

class Renderer(val layer: SkiaLayer) : SkiaRenderer {
    val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    val font = Font(typeface, 40f)
    val paint = Paint().apply {
        color = 0xff9BC730L.toInt()
        mode = PaintMode.FILL
        strokeWidth = 1f
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()

        // РИСОВАНИЕ

        layer.needRedraw()
    }
}

object State {
    var mouseX = 0f
    var mouseY = 0f
}

object MyMouseMotionAdapter : MouseMotionAdapter() {
    override fun mouseMoved(event: MouseEvent) {
        State.mouseX = event.x.toFloat()
        State.mouseY = event.y.toFloat()
    }
}
