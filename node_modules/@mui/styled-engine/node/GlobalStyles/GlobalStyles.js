"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = GlobalStyles;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _react2 = require("@emotion/react");
var _jsxRuntime = require("react/jsx-runtime");
function isEmpty(obj) {
  return obj === undefined || obj === null || Object.keys(obj).length === 0;
}
function GlobalStyles(props) {
  const {
    styles,
    defaultTheme = {}
  } = props;
  const globalStyles = typeof styles === 'function' ? themeInput => styles(isEmpty(themeInput) ? defaultTheme : themeInput) : styles;
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_react2.Global, {
    styles: globalStyles
  });
}
process.env.NODE_ENV !== "production" ? GlobalStyles.propTypes = {
  defaultTheme: _propTypes.default.object,
  styles: _propTypes.default.oneOfType([_propTypes.default.array, _propTypes.default.string, _propTypes.default.object, _propTypes.default.func])
} : void 0;