# `XPathEvaluator`

The `xpath.parse()` method returns an `XPathEvaluator`, which contains the following methods.

Each of these methods takes an optional `options` object, which can contain any of the following properties:

`namespaces` - a namespace resolver. See the [documentation page](namespace%20resolvers.md) for details.

`variables` - a variable resolver. See the [documentation page](variable%20resolvers.md) for details.

`functions` - a function resolver. See the [documentation page](function%20resolvers.md) for details.

`node` - the context node for evaluating the expression

Example usage: 

```js
var evaluator = xpath.parse('/characters/character[@greeting = $greeting]');
var character = evaluator.select1({
    node: myCharacterDoc,
    variables: {
        greeting: "Hello, I'm Harry, Harry Potter."
    }
});
```

## `XPathEvaluator` methods

`evaluate([options])`

Evaluates the XPath expression and returns the result. The resulting type is determined based on the type of the expression, using the same criteria as [`xpath.select`](xpath%20methods.md).

`evaluateNumber([options])`

Evaluates the XPath expression and returns the result as a number.

`evaluateString([options])`

Evaluates the XPath expression and returns the result as a string.

`evaluateBoolean([options])`

Evaluates the XPath expression and returns the result as a boolean value.

`evaluateNodeSet([options])`

Evaluates the XPath expression and returns the result as an XNodeSet. See the [documentation page](#) for details on this interface.

This is only valid for expressions that evaluate to a node set.

`select([options])`

Evaluates the XPath expression and returns an array of the resulting nodes, in document order.

This is only valid for expressions that evaluate to a node set.

`select1([options])`

Evaluates the XPath expression and the first node in the resulting node set, in document order. Returns `undefined` 

This is only valid for expressions that evaluate to a node set.

