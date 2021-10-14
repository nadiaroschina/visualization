import kotlin.test.*

internal class TestQuery {

    @Test
    fun testParseArgs() {
        assertFailsWith<EmptyQuery> { parseArgs(arrayOf()) }
        assertFailsWith<NoArguments> { parseArgs(arrayOf("round")) }
        assertFailsWith<UnsupportedDiagram> { parseArgs(arrayOf("Unicorns", "pink", "15")) }
        assertFailsWith<InvalidArgumentsNumber> { parseArgs(arrayOf("histogram", "blue", "11", "green")) }
        assertFailsWith<InvalidArgument> { parseArgs(arrayOf("scatterplot", "white", "black")) }
        assertFailsWith<NegativeArgument> { parseArgs(arrayOf("round", "white", "-11")) }

        val expected = Query(DiagramType.ScatterPlot,
            listOf(Element("gold", 16.7f), Element("rose", -1.1f)))
        val input = arrayOf("scatterplot", "gold", "16.7", "rose", "-1.1")
        assertEquals(expected, parseArgs(input))
    }
}
