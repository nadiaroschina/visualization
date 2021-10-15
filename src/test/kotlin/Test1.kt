import java.io.File
import kotlin.test.*

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
    }
}
