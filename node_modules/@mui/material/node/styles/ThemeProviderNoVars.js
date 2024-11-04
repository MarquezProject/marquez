"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = ThemeProviderNoVars;
var React = _interopRequireWildcard(require("react"));
var _system = require("@mui/system");
var _identifier = _interopRequireDefault(require("./identifier"));
var _jsxRuntime = require("react/jsx-runtime");
function ThemeProviderNoVars({
  theme: themeInput,
  ...props
}) {
  const scopedTheme = _identifier.default in themeInput ? themeInput[_identifier.default] : undefined;
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_system.ThemeProvider, {
    ...props,
    themeId: scopedTheme ? _identifier.default : undefined,
    theme: scopedTheme || themeInput
  });
}