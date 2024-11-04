"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = useTheme;
var React = _interopRequireWildcard(require("react"));
var _ThemeContext = _interopRequireDefault(require("./ThemeContext"));
function useTheme() {
  const theme = React.useContext(_ThemeContext.default);
  if (process.env.NODE_ENV !== 'production') {
    // TODO: uncomment once we enable eslint-plugin-react-compiler eslint-disable-next-line react-compiler/react-compiler
    // eslint-disable-next-line react-hooks/rules-of-hooks -- It's not required to run React.useDebugValue in production
    React.useDebugValue(theme);
  }
  return theme;
}