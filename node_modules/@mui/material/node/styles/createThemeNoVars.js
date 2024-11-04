"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createMuiTheme = createMuiTheme;
exports.default = void 0;
var _formatMuiErrorMessage2 = _interopRequireDefault(require("@mui/utils/formatMuiErrorMessage"));
var _deepmerge = _interopRequireDefault(require("@mui/utils/deepmerge"));
var _styleFunctionSx = _interopRequireWildcard(require("@mui/system/styleFunctionSx"));
var _createTheme = _interopRequireDefault(require("@mui/system/createTheme"));
var _generateUtilityClass = _interopRequireDefault(require("@mui/utils/generateUtilityClass"));
var _createMixins = _interopRequireDefault(require("./createMixins"));
var _createPalette = _interopRequireDefault(require("./createPalette"));
var _createTypography = _interopRequireDefault(require("./createTypography"));
var _shadows = _interopRequireDefault(require("./shadows"));
var _createTransitions = _interopRequireDefault(require("./createTransitions"));
var _zIndex = _interopRequireDefault(require("./zIndex"));
var _stringifyTheme = require("./stringifyTheme");
function createThemeNoVars(options = {}, ...args) {
  const {
    breakpoints: breakpointsInput,
    mixins: mixinsInput = {},
    spacing: spacingInput,
    palette: paletteInput = {},
    transitions: transitionsInput = {},
    typography: typographyInput = {},
    shape: shapeInput,
    ...other
  } = options;
  if (options.vars) {
    throw new Error(process.env.NODE_ENV !== "production" ? 'MUI: `vars` is a private field used for CSS variables support.\n' + 'Please use another name.' : (0, _formatMuiErrorMessage2.default)(20));
  }
  const palette = (0, _createPalette.default)(paletteInput);
  const systemTheme = (0, _createTheme.default)(options);
  let muiTheme = (0, _deepmerge.default)(systemTheme, {
    mixins: (0, _createMixins.default)(systemTheme.breakpoints, mixinsInput),
    palette,
    // Don't use [...shadows] until you've verified its transpiled code is not invoking the iterator protocol.
    shadows: _shadows.default.slice(),
    typography: (0, _createTypography.default)(palette, typographyInput),
    transitions: (0, _createTransitions.default)(transitionsInput),
    zIndex: {
      ..._zIndex.default
    }
  });
  muiTheme = (0, _deepmerge.default)(muiTheme, other);
  muiTheme = args.reduce((acc, argument) => (0, _deepmerge.default)(acc, argument), muiTheme);
  if (process.env.NODE_ENV !== 'production') {
    // TODO v6: Refactor to use globalStateClassesMapping from @mui/utils once `readOnly` state class is used in Rating component.
    const stateClasses = ['active', 'checked', 'completed', 'disabled', 'error', 'expanded', 'focused', 'focusVisible', 'required', 'selected'];
    const traverse = (node, component) => {
      let key;

      // eslint-disable-next-line guard-for-in
      for (key in node) {
        const child = node[key];
        if (stateClasses.includes(key) && Object.keys(child).length > 0) {
          if (process.env.NODE_ENV !== 'production') {
            const stateClass = (0, _generateUtilityClass.default)('', key);
            console.error([`MUI: The \`${component}\` component increases ` + `the CSS specificity of the \`${key}\` internal state.`, 'You can not override it like this: ', JSON.stringify(node, null, 2), '', `Instead, you need to use the '&.${stateClass}' syntax:`, JSON.stringify({
              root: {
                [`&.${stateClass}`]: child
              }
            }, null, 2), '', 'https://mui.com/r/state-classes-guide'].join('\n'));
          }
          // Remove the style to prevent global conflicts.
          node[key] = {};
        }
      }
    };
    Object.keys(muiTheme.components).forEach(component => {
      const styleOverrides = muiTheme.components[component].styleOverrides;
      if (styleOverrides && component.startsWith('Mui')) {
        traverse(styleOverrides, component);
      }
    });
  }
  muiTheme.unstable_sxConfig = {
    ..._styleFunctionSx.unstable_defaultSxConfig,
    ...other?.unstable_sxConfig
  };
  muiTheme.unstable_sx = function sx(props) {
    return (0, _styleFunctionSx.default)({
      sx: props,
      theme: this
    });
  };
  muiTheme.toRuntimeSource = _stringifyTheme.stringifyTheme; // for Pigment CSS integration

  return muiTheme;
}
let warnedOnce = false;
function createMuiTheme(...args) {
  if (process.env.NODE_ENV !== 'production') {
    if (!warnedOnce) {
      warnedOnce = true;
      console.error(['MUI: the createMuiTheme function was renamed to createTheme.', '', "You should use `import { createTheme } from '@mui/material/styles'`"].join('\n'));
    }
  }
  return createThemeNoVars(...args);
}
var _default = exports.default = createThemeNoVars;