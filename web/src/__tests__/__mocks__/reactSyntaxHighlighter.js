const React = require('react')

function SyntaxHighlighter(props) {
  return React.createElement('pre', null, props.children)
}

module.exports = SyntaxHighlighter
