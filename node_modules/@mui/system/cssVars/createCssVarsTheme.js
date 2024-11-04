"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _prepareCssVars = _interopRequireDefault(require("./prepareCssVars"));
var _getColorSchemeSelector = require("./getColorSchemeSelector");
var _InitColorSchemeScript = require("../InitColorSchemeScript/InitColorSchemeScript");
function createCssVarsTheme({
  colorSchemeSelector = `[${_InitColorSchemeScript.DEFAULT_ATTRIBUTE}="%s"]`,
  ...theme
}) {
  const output = theme;
  const result = (0, _prepareCssVars.default)(output, {
    ...theme,
    prefix: theme.cssVarPrefix,
    colorSchemeSelector
  });
  output.vars = result.vars;
  output.generateThemeVars = result.generateThemeVars;
  output.generateStyleSheets = result.generateStyleSheets;
  output.colorSchemeSelector = colorSchemeSelector;
  output.getColorSchemeSelector = (0, _getColorSchemeSelector.createGetColorSchemeSelector)(colorSchemeSelector);
  return output;
}
var _default = exports.default = createCssVarsTheme;