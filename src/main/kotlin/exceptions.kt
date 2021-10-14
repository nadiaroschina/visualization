class EmptyQuery() :
    Exception("empty query")

class NoArguments():
    Exception("can't draw a diagram with no elements")

class UnsupportedDiagram(name: String):
    Exception("diagram type \"$name\" is not supported")

class InvalidArgumentsNumber():
    Exception("number of arguments must be even")

class InvalidArgument(name: String, valueStr: String):
    Exception("value of $name expected to be a number, but was $valueStr")
