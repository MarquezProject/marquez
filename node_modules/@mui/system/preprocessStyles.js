"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = preprocessStyles;
var _styledEngine = require("@mui/styled-engine");
function preprocessStyles(input) {
  const {
    variants,
    ...style
  } = input;
  const result = {
    variants,
    style: (0, _styledEngine.internal_serializeStyles)(style),
    isProcessed: true
  };

  // Not supported on styled-components
  if (result.style === style) {
    return result;
  }
  if (variants) {
    variants.forEach(variant => {
      if (typeof variant.style !== 'function') {
        variant.style = (0, _styledEngine.internal_serializeStyles)(variant.style);
      }
    });
  }
  return result;
}