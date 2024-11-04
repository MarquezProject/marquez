"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = useTheme;
var React = _interopRequireWildcard(require("react"));
var _system = require("@mui/system");
var _defaultTheme = _interopRequireDefault(require("./defaultTheme"));
var _identifier = _interopRequireDefault(require("./identifier"));
function useTheme() {
  const theme = (0, _system.useTheme)(_defaultTheme.default);
  if (process.env.NODE_ENV !== 'production') {
    // TODO: uncomment once we enable eslint-plugin-react-compiler // eslint-disable-next-line react-compiler/react-compiler
    // eslint-disable-next-line react-hooks/rules-of-hooks
    React.useDebugValue(theme);
  }
  return theme[_identifier.default] || theme;
}