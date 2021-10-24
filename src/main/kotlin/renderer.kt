import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import java.io.File
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
        color = 0xff082B82.toInt()  // dark blue
        mode = PaintMode.FILL
        strokeWidth = 2f
    }
    private val strokePaint = Paint().apply {
        color = 0xff082B82.toInt()  // dark blue
        mode = PaintMode.STROKE
        strokeWidth = 2f
    }

    var data: Data = emptyList()
    var diagramType: DiagramType? = null
    var file: File? = null

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
        val eps = min(width, height) / 20
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Integer.min(width, height) / 2f - eps

        // drawing the circle
        canvas.drawCircle(centerX, centerY, radius, strokePaint)

        // calculating sum of all values
        var sumVal = 0f
        data.forEach {
            sumVal += it.value
        }

        // drawing the diagram itself
        var angleRad = 0.5 * Math.PI    // stores the angle of the right bound of current sector in radians
        val currPaint = Paint().apply {
            color = 0xf00000000.toInt()
            mode = PaintMode.FILL
            strokeWidth = 2f
        }

        data.forEach {
            val size = it.value / sumVal    //  size of sector in percentages
            val deltaRad = size * 2 * Math.PI    //  turning angle in radians

            // filling sector
            currPaint.color = ((it.value / sumVal) * 100000 + 0x80ff0000.toInt()).toInt()
            canvas.drawArc(
                centerX - radius, centerY - radius,
                centerX + radius, centerY + radius,
                radiansToDegrees(-angleRad.toFloat()), radiansToDegrees(-deltaRad.toFloat()),
                true, currPaint
            )
            // drawing separator line
            canvas.drawLine(
                centerX, centerY,
                centerX + radius * cos(angleRad).toFloat(), centerY - radius * sin(angleRad).toFloat(),
                mainPaint
            )

            // writing text
            writeInSector(canvas, centerX, centerY, (angleRad + deltaRad / 2).toFloat(), radius, it, mainPaint)

            angleRad += deltaRad
        }

    }


    private fun writeInSector(
        canvas: Canvas, centerX: Float, centerY: Float,
        angle: Float, radius: Float, element: Element,
        paint: Paint
    ) {
        val eps = 1f * font.size
        val textEps = (font.size * element.type.length) / 4
        canvas.drawString(
            element.type,
            centerX - textEps + (radius * 0.5f) * cos(angle),
            centerY - (radius * 0.5f) * sin(angle),
            font,
            paint
        )
        canvas.drawString(
            element.value.toString(),
            centerX - textEps + (radius * 0.5f) * cos(angle),
            centerY - (radius * 0.5f) * sin(angle) + eps,
            font,
            paint
        )
    }


    private fun histogram(canvas: Canvas, width: Int, height: Int) {

        // determining boundaries
        val bigEps = min(width, height) / 10f
        val smallEps = min(width, height) / 60f

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
        canvas.drawLine(bigEps / 2, zeroY, width - bigEps / 2, zeroY, mainPaint)
        canvas.drawLine(bigEps, bigEps / 2, bigEps, height - bigEps / 2, mainPaint)
        // little triangles on them
        val d1 = 7f
        val d2 = 15f
        canvas.drawLine(bigEps - d1, bigEps / 2 + d2, bigEps, bigEps / 2, mainPaint)
        canvas.drawLine(bigEps + d1, bigEps / 2 + d2, bigEps, bigEps / 2, mainPaint)
        canvas.drawLine(width - bigEps / 2 - d2, zeroY - d1, width - bigEps / 2, zeroY, mainPaint)
        canvas.drawLine(width - bigEps / 2 - d2, zeroY + d1, width - bigEps / 2, zeroY, mainPaint)
        // scale on Oy
        val scalesCount = 10
        val scaleFont = Font(typeface, 12f)
        for (ind in 0..floor(scalesCount * maxVal / rangeY).toInt()) {
            val value = (ind * rangeY) / scalesCount
            val y = zeroY - (ind * (height - 2 * bigEps)) / scalesCount
            canvas.drawLine(bigEps - d1, y, bigEps, y, mainPaint)
            canvas.drawString(String.format("%.1f", value), smallEps, y, scaleFont, mainPaint)
        }
        for (ind in 1..floor(scalesCount * abs(minVal) / rangeY).toInt()) {
            val value = -(ind * rangeY) / scalesCount
            val y = zeroY + (ind * (height - 2 * bigEps)) / scalesCount
            canvas.drawLine(bigEps - d1, y, bigEps, y, mainPaint)
            canvas.drawString(String.format("%.1f", value), smallEps, y, scaleFont, mainPaint)
        }


        // drawing rectangles to match values
        var left = bigEps + smallEps
        var right = bigEps + sizeX - smallEps
        val currPaint = Paint().apply {
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
            currPaint.color = ((it.value / rangeY) * 10000 + 0x80ff0000.toInt()).toInt()

            val rect = Rect(left, max(y, zeroY), right, min(y, zeroY))
            canvas.drawRect(rect, currPaint)

            // adding text
            val textEps = font.size
            val textX = left + smallEps
            val textY = zeroY + textEps * (it.value.sign)
            canvas.drawString(it.type, textX, textY, font, mainPaint)

            // moving to the next rectangle
            left += sizeX
            right += sizeX
        }
    }


}

private fun scatterplot(canvas: Canvas, width: Int, height: Int) {
    // TODO: 15.10.2021
}

