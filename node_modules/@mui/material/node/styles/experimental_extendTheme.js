"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = deprecatedExtendTheme;
var _createThemeWithVars = _interopRequireDefault(require("./createThemeWithVars"));
let warnedOnce = false;
function deprecatedExtendTheme(...args) {
  if (!warnedOnce) {
    console.warn(['MUI: The `experimental_extendTheme` has been stabilized.', '', "You should use `import { extendTheme } from '@mui/material/styles'`"].join('\n'));
    warnedOnce = true;
  }
  return (0, _createThemeWithVars.default)(...args);
}