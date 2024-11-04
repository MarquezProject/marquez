"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = getValidReactChildren;
var React = _interopRequireWildcard(require("react"));
/**
 * Gets only the valid children of a component,
 * and ignores any nullish or falsy child.
 *
 * @param children the children
 */
function getValidReactChildren(children) {
  return React.Children.toArray(children).filter(child => /*#__PURE__*/React.isValidElement(child));
}