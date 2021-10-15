import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.SkiaWindow
import org.jetbrains.skiko.toBufferedImage
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.io.File
import javax.imageio.ImageIO
import javax.swing.WindowConstants


enum class DiagramType(val str: String) {
    Round("round"), Histogram("histogram"), ScatterPlot("scatterplot")
}

typealias Type = String
typealias Value = Float

data class Element(val type: Type, val value: Value)
typealias Data = List<Element>


fun main(args: Array<String>) {

    // processing input
    val query = parseArgs(args)

    // window
    createWindow("pf-2021-viz", query.diagramType, query.data, query.file)
}

fun createWindow(title: String, diagramType: DiagramType, data: Data, file: File) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = Renderer(window.layer)
    (window.layer.renderer as Renderer).data = data
    (window.layer.renderer as Renderer).diagramType = diagramType
    (window.layer.renderer as Renderer).file = file

    window.layer.addMouseMotionListener(MyMouseMotionAdapter)

    window.preferredSize = Dimension(600, 600)
    window.minimumSize = Dimension(200, 200)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true

    val bitImage = window.layer.screenshot()
    val image = bitImage?.toBufferedImage()
    ImageIO.write(image, "png", file)
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
