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
import java.lang.Integer.min
import javax.swing.WindowConstants
import kotlin.math.*

enum class DiagramType(val str: String) {
    Round("round"), Histogram("histogram"), ScatterPlot("scatterplot")
}

typealias Type = String
typealias Value = Float

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
        val value: Float = args[i + 1].toFloatOrNull() ?: throw InvalidArgument(type, args[i + 1])
        if (diagramType == DiagramType.Round && value < 0) {
            throw NegativeArgument(diagramType, type, value)
        }
        data.add(Element(type, value))
    }
    return Query(diagramType, data)
}


fun main(args: Array<String>) {

//    val query = parseArgs(args)
//    println(query.diagramType)
//    println(query.data)

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

class Renderer(private val layer: SkiaLayer) : SkiaRenderer {
    private val typeface = Typeface.makeFromName("Times New Roman", FontStyle.NORMAL)
    private val font = Font(typeface, 16f)
    private val mainPaint = Paint().apply {
        color = 0xff22D6E5.toInt()
        mode = PaintMode.STROKE
        strokeWidth = 2f
    }
    private val textPaint = Paint().apply {
        color = 0xff082B82.toInt()
        mode = PaintMode.FILL
        strokeWidth = 2f
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()

        roundDiagram(
            canvas,
            listOf(
                Element("apples", 30.0f),
                Element("oranges", 30.0f),
                Element("bananas", 40.0f)
            ),
            w, h
        )

        layer.needRedraw()
    }

    // draws a round diagram
    private fun roundDiagram(canvas: Canvas, data: Data, width: Int, height: Int) {
        // drawing the circle
        val eps = 30F
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) / 4f - eps
        canvas.drawCircle(centerX, centerY, radius, mainPaint)

        // calculating sum of all values
        val count = data.size
        var sumVal = 0f
        data.forEach {
            sumVal += it.value
        }

        // drawing the diagram itself
        var angle = 0.5 * Math.PI    // stores the angle of the right bound of current sector
        var fillPaint = Paint().apply {
            color = 0xff9BC730L.toInt() // green
            mode = PaintMode.FILL
            strokeWidth = 2f
        } // changing color depending on sector size todo
        data.forEach {
            val size = it.value / sumVal    // current size of the sector in percentages
            val delta = size * 2 * Math.PI    // current turning angle

            // writing text
            writeInSector(canvas, centerX, centerY, (angle - delta / 2).toFloat(), radius, it)

            // filling sector - todo

            //drawing radius
            drawRadius(canvas, centerX, centerY, (angle - delta).toFloat(), radius)

            angle -= delta
        }

    }

    private fun drawRadius(
        canvas: Canvas, centerX: Float, centerY: Float,
        angle: Float, radius: Float
    ) {
        canvas.drawLine(
            centerX,
            centerY,
            centerX + radius * cos(angle),
            centerY - radius * sin(angle),
            mainPaint
        )
    }


    private fun writeInSector(
        canvas: Canvas, centerX: Float, centerY: Float,
        angle: Float, radius: Float, element: Element
    ) {
        val eps = 2 * font.size
        canvas.drawString(
            element.type,
            centerX - eps + (radius * 1.5f) * cos(angle),
            centerY - (radius * 1.5f) * sin(angle),
            font,
            textPaint
        )
        canvas.drawString(
            element.value.toString(),
            centerX - eps + (radius * 1.5f) * cos(angle),
            centerY - (radius * 1.5f) * sin(angle) + eps,
            font,
            textPaint
        )
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
