import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import kotlin.math.*

class Renderer(private val layer: SkiaLayer) : SkiaRenderer {
    private val typeface = Typeface.makeFromName("Times New Roman", FontStyle.NORMAL)
    private val font = Font(typeface, 16f)
    private val mainPaint = Paint().apply {
        color = 0xff082B82.toInt()
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

        histogram(
            canvas,
            listOf(
                Element("apples", 30f),
                Element("oranges", -60f),
                Element("bananas", 40f),
                Element("melons", -20f),
                Element("peaches", 100f)
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
        val radius = Integer.min(width, height) / 2f - eps
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
            val size = it.value / sumVal    //  size of sector in percentages
            val delta = size * 2 * Math.PI    //  turning angle

            // writing text
            writeInSector(canvas, centerX, centerY, (angle - delta / 2).toFloat(), radius, it)

            // filling sector
            fillSector(canvas, centerX, centerY, angle.toFloat(), delta.toFloat(), radius, fillPaint)

            //drawing radius
            drawRadius(canvas, centerX, centerY, (angle - delta).toFloat(), radius, mainPaint)

            angle -= delta
        }

    }

    private fun drawRadius(
        canvas: Canvas, centerX: Float, centerY: Float,
        angle: Float, radius: Float, paint: Paint
    ) {
        canvas.drawLine(
            centerX,
            centerY,
            centerX + radius * cos(angle),
            centerY - radius * sin(angle),
            paint
        )
    }


    private fun writeInSector(
        canvas: Canvas, centerX: Float, centerY: Float,
        angle: Float, radius: Float, element: Element
    ) {
        val eps = 1f * font.size
        val dx = (font.size * element.type.length) / 4
        canvas.drawString(
            element.type,
            centerX - dx +(radius * 0.5f) * cos(angle),
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

    private fun fillSector(
        canvas: Canvas, centerX: Float, centerY: Float,
        angle: Float, delta: Float, radius: Float, paint: Paint
    ) {
        // TODO: 15.10.2021
    }

    private fun histogram(canvas: Canvas, data: Data, width: Int, height: Int) {

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
        var paint = Paint().apply {
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
            paint.color = ((it.value / rangeY) * 10000 + 0x80ff0000.toInt()).toInt()

            val rect = Rect(left, max(y, zeroY), right, min(y, zeroY))
            canvas.drawRect(rect, paint)

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

}