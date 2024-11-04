"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = ThemeProvider;
var React = _interopRequireWildcard(require("react"));
var _ThemeProviderNoVars = _interopRequireDefault(require("./ThemeProviderNoVars"));
var _ThemeProviderWithVars = require("./ThemeProviderWithVars");
var _identifier = _interopRequireDefault(require("./identifier"));
var _jsxRuntime = require("react/jsx-runtime");
function ThemeProvider({
  theme,
  ...props
}) {
  if (typeof theme === 'function') {
    return /*#__PURE__*/(0, _jsxRuntime.jsx)(_ThemeProviderNoVars.default, {
      theme: theme,
      ...props
    });
  }
  const muiTheme = _identifier.default in theme ? theme[_identifier.default] : theme;
  if (!('colorSchemes' in muiTheme)) {
    return /*#__PURE__*/(0, _jsxRuntime.jsx)(_ThemeProviderNoVars.default, {
      theme: theme,
      ...props
    });
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_ThemeProviderWithVars.CssVarsProvider, {
    theme: theme,
    ...props
  });
}