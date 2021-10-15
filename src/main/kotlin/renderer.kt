import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import kotlin.math.*

fun degreesToRadians(x: Float): Float {
    return (x * Math.PI / 180).toFloat()
}

fun radiansToDegrees(x: Float): Float {
    return (x * 180 / Math.PI).toFloat()
}

class Renderer(private val layer: SkiaLayer) : SkiaRenderer {
    private val typeface = Typeface.makeFromName("Courier", FontStyle.NORMAL)
    private val font = Font(typeface, 16f)
    private val mainPaint = Paint().apply {
        color = 0xff082B82.toInt() // dark blue
        mode = PaintMode.STROKE
        strokeWidth = 2f
    }
    private val textPaint = Paint().apply {
        color = 0xff082B82.toInt()  // dark blue
        mode = PaintMode.FILL
        strokeWidth = 2f
    }
    var data: Data = emptyList()
    var diagramType: DiagramType? = null

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()

        when (diagramType) {
            DiagramType.Round -> roundDiagram(canvas, w, h)
            DiagramType.Histogram -> histogram(canvas, w, h)
            DiagramType.ScatterPlot -> scatterplot(canvas, w, h)
        }

        layer.needRedraw()
    }

    // draws a round diagram
    private fun roundDiagram(canvas: Canvas, width: Int, height: Int) {

        // determining parameters
        val eps = 30F
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Integer.min(width, height) / 2f - eps

        // drawing the circle
        canvas.drawCircle(centerX, centerY, radius, mainPaint)

        // calculating sum of all values
        val count = data.size
        var sumVal = 0f
        data.forEach {
            sumVal += it.value
        }

        // drawing the diagram itself
        var angle = 0.5 * Math.PI    // stores the angle of the right bound of current sector
        val fillPaint = Paint().apply {
            color = 0xff9BC730L.toInt() // green
            mode = PaintMode.STROKE_AND_FILL
            strokeWidth = 2f
        }

        data.forEach {
            val size = it.value / sumVal    //  size of sector in percentages
            val delta = size * 2 * Math.PI    //  turning angle

            // writing text
            writeInSector(canvas, centerX, centerY, (angle - delta / 2).toFloat(), radius, it)

            // filling sector
            fillPaint.color = ((it.value / sumVal) * 100000 + 0x80ff0000.toInt()).toInt()
//            canvas.drawArc(
//                centerX - radius, centerY - radius,
//                centerX + radius, centerY + radius,
//                radiansToDegrees(angle.toFloat()) + 180, radiansToDegrees(delta.toFloat()),
//                true, fillPaint
//            )
            // TODO: 15.10.2021  

            //drawing radius
            //canvas.drawLine(centerX, centerY, centerX + cos(angle))

            angle -= delta
        }

    }


    private fun writeInSector(
        canvas: Canvas, centerX: Float, centerY: Float,
        angle: Float, radius: Float, element: Element
    ) {
        val eps = 1f * font.size
        val dx = (font.size * element.type.length) / 4
        canvas.drawString(
            element.type,
            centerX - dx + (radius * 0.5f) * cos(angle),
            centerY - (radius * 0.5f) * sin(angle),
            font,
            textPaint
        )
        canvas.drawString(
            element.value.toString(),
            centerX - dx + (radius * 0.5f) * cos(angle),
            centerY - (radius * 0.5f) * sin(angle) + eps,
            font,
            textPaint
        )
    }


    private fun histogram(canvas: Canvas, width: Int, height: Int) {

        // determining boundaries
        val bigEps = 40f
        val smallEps = 10f

        // max and min values in data
        var maxVal = 0f
        var minVal = 0f
        data.forEach {
            maxVal = max(maxVal, it.value)
            minVal = min(minVal, it.value)
        }

        val rangeY = maxVal - minVal
        val zeroY = bigEps + (height - 2 * bigEps) * maxVal / rangeY    // Oy absolute coordinate
        val sizeX = (width - 2 * bigEps) / data.size    // rectangle length

        // Ox and Oy lines
        canvas.drawLine(bigEps, zeroY, width - bigEps, zeroY, mainPaint)
        canvas.drawLine(bigEps, bigEps, bigEps, height - bigEps, mainPaint)


        // drawing rectangles to match values
        var left = bigEps + smallEps
        var right = bigEps + sizeX - smallEps
        val fillPaint = Paint().apply {
            color = 0x80000000.toInt() // green
            mode = PaintMode.FILL
            strokeWidth = 2f
        }
        data.forEach {
            // determining top/bottom coordinate
            val y = if (it.value > 0) {
                zeroY - (height - 2 * bigEps) * (it.value / rangeY)
            } else {
                zeroY + (height - 2 * bigEps) * (abs(it.value) / rangeY)
            }

            // adjusting the color
            fillPaint.color = ((it.value / rangeY) * 10000 + 0x80ff0000.toInt()).toInt()

            val rect = Rect(left, max(y, zeroY), right, min(y, zeroY))
            canvas.drawRect(rect, fillPaint)

            // adding text
            val textEps = font.size
            val textX = left + smallEps
            val textY = zeroY + textEps * (it.value.sign)
            canvas.drawString(it.type, textX, textY, font, textPaint)

            // moving to the next rectangle
            left += sizeX
            right += sizeX
        }


    }

    private fun scatterplot(canvas: Canvas, width: Int, height: Int) {
        // TODO: 15.10.2021
    }

}