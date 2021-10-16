class EmptyQuery() :
    Exception("empty query")

class NotEnoughArguments():
    Exception("expected diagram type, parameters and file name")

class UnsupportedDiagram(name: String):
    Exception("diagram type \"$name\" is not supported")

class InvalidArgumentsNumber():
    Exception("expected diagram type, parameters and file name")

class InvalidArgument(name: String, valueStr: String):
    Exception("value of $name expected to be a number, but was $valueStr")

class NegativeArgument(diagramType: DiagramType, name: String, value: Float):
    Exception("value of $name expected to be a positive number in $diagramType diagram type, but was $value")

