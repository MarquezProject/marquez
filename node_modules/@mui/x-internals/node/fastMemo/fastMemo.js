"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.fastMemo = fastMemo;
var React = _interopRequireWildcard(require("react"));
var _fastObjectShallowCompare = require("../fastObjectShallowCompare");
function fastMemo(component) {
  return /*#__PURE__*/React.memo(component, _fastObjectShallowCompare.fastObjectShallowCompare);
}