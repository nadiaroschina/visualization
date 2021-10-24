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


fun main(args: Array<String>) {

    // processing input
    val query = parseArgs(args)

    // window
    createWindow("pf-2021-viz", query)
}

fun createWindow(title: String, query: Query) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    applyData(window, query)

    window.layer.addMouseMotionListener(MyMouseMotionAdapter)

    window.preferredSize = Dimension(600, 600)
    window.minimumSize = Dimension(200, 200)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true

    val bitImage = window.layer.screenshot()
    val image = bitImage?.toBufferedImage()
    ImageIO.write(image, "png", query.file)
}

fun applyData(window: SkiaWindow, query: Query) {
    val renderer = Renderer(window.layer)
    renderer.data = query.data
    renderer.diagramType = query.diagramType
    renderer.file = query.file
    window.layer.renderer = renderer
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
