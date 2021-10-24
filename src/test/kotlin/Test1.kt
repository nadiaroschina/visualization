import org.jetbrains.skiko.toBitmap
import org.jetbrains.skiko.toImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.E
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class TestQuery {

    @Test
    fun testParseArgs() {
        assertFailsWith<EmptyQuery> { parseArgs(arrayOf()) }
        assertFailsWith<NotEnoughArguments> { parseArgs(arrayOf("round")) }
        assertFailsWith<UnsupportedDiagram> { parseArgs(arrayOf("Unicorns", "pink", "15")) }
        assertFailsWith<InvalidArgumentsNumber> { parseArgs(arrayOf("histogram", "blue", "11", "green", "path1")) }
        assertFailsWith<InvalidArgument> { parseArgs(arrayOf("scatterplot", "white", "black", "path1")) }
        assertFailsWith<NegativeArgument> { parseArgs(arrayOf("round", "white", "-11", "path1")) }

        val expected = Query(
            DiagramType.ScatterPlot,
            listOf(Element("gold", 16f), Element("rose", -1.7f)),
            File("src/data/img1")
        )
        val input = arrayOf("scatterplot", "gold", "16", "rose", "-1.7", "src/data/img1")
        assertEquals(expected, parseArgs(input))

        File("src/data/ing1").deleteRecursively()
    }

    @Test
    fun testRoundDiagram1() {
        val tmpFile = File("src/data/tmp.png")
        val query = Query(
            DiagramType.Round,
            listOf(
                Element("apples", 78.3f), Element("grapes", 101.4f),
                Element("oranges", 44.4f), Element("plums", 180.3f)
            ),
            tmpFile
        )
        createWindow("test", query)

        val realFile = File("src/data/round1.png")

        val realImage = ImageIO.read(realFile).toImage().encodeToData()
        val tmpImage = ImageIO.read(tmpFile).toImage().encodeToData()

        assertEquals(realImage, tmpImage)

        tmpFile.deleteRecursively()
    }

    @Test
    fun testRoundDiagram2() {
        val tmpFile = File("src/data/tmp.png")
        val query = Query(
            DiagramType.Round,
            listOf(
                Element("A", 83f), Element("B", 54f), Element("C", 94f),
                Element("D", 30f), Element("E", 77f), Element("F", 31f),
                Element("G", 36f), Element("H", 87f), Element("I", 74f),
                Element("J", 67f)
            ),
            tmpFile
        )
        createWindow("test", query)

        val realFile = File("src/data/round2.png")

        val realImage = ImageIO.read(realFile).toImage().encodeToData()
        val tmpImage = ImageIO.read(tmpFile).toImage().encodeToData()

        assertEquals(realImage, tmpImage)

        tmpFile.deleteRecursively()
    }

    @Test
    fun testHistogram1() {
        val tmpFile = File("src/data/tmp.png")
        val query = Query(
            DiagramType.Histogram,
            listOf(
                Element("daisies", 87.9f), Element("peonies", -60.0f),
                Element("lilies", 20.0f), Element("roses",  12.9f),
                Element("asters", 28.5f)
            ),
            tmpFile
        )
        createWindow("test", query)

        val realFile = File("src/data/histo1.png")

        val realImage = ImageIO.read(realFile).toImage().encodeToData()
        val tmpImage = ImageIO.read(tmpFile).toImage().encodeToData()

        assertEquals(realImage, tmpImage)

        tmpFile.deleteRecursively()
    }

    @Test
    fun testHistogram2() {
        val tmpFile = File("src/data/tmp.png")
        val query = Query(
            DiagramType.Histogram,
            listOf(
                Element("A", 30f), Element("B", 51f), Element("C", 35f),
                Element("D", 59f), Element("E", 81f), Element("F", 30F),
                Element("G", 63f), Element("H", 96f), Element("I", 51f),
                Element("J", 71f)
            ),
            tmpFile
        )
        createWindow("test", query)

        val realFile = File("src/data/histo2.png")

        val realImage = ImageIO.read(realFile).toImage().encodeToData()
        val tmpImage = ImageIO.read(tmpFile).toImage().encodeToData()

        assertEquals(realImage, tmpImage)

        tmpFile.deleteRecursively()
    }

}
