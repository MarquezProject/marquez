"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {
  experimental_sx: true,
  THEME_ID: true,
  adaptV4Theme: true,
  hexToRgb: true,
  rgbToHex: true,
  hslToRgb: true,
  decomposeColor: true,
  recomposeColor: true,
  getContrastRatio: true,
  getLuminance: true,
  emphasize: true,
  alpha: true,
  darken: true,
  lighten: true,
  css: true,
  keyframes: true,
  StyledEngineProvider: true,
  unstable_createBreakpoints: true,
  createTheme: true,
  createMuiTheme: true,
  unstable_createMuiStrictModeTheme: true,
  createStyles: true,
  unstable_getUnit: true,
  unstable_toUnitless: true,
  responsiveFontSizes: true,
  createTransitions: true,
  duration: true,
  easing: true,
  createColorScheme: true,
  useTheme: true,
  useThemeProps: true,
  styled: true,
  experimentalStyled: true,
  ThemeProvider: true,
  makeStyles: true,
  withStyles: true,
  withTheme: true,
  extendTheme: true,
  experimental_extendTheme: true,
  getOverlayAlpha: true,
  shouldSkipGeneratingVar: true,
  private_createTypography: true,
  private_createMixins: true,
  private_excludeVariablesFromRoot: true
};
Object.defineProperty(exports, "StyledEngineProvider", {
  enumerable: true,
  get: function () {
    return _system.StyledEngineProvider;
  }
});
Object.defineProperty(exports, "THEME_ID", {
  enumerable: true,
  get: function () {
    return _identifier.default;
  }
});
Object.defineProperty(exports, "ThemeProvider", {
  enumerable: true,
  get: function () {
    return _ThemeProvider.default;
  }
});
Object.defineProperty(exports, "adaptV4Theme", {
  enumerable: true,
  get: function () {
    return _adaptV4Theme.default;
  }
});
Object.defineProperty(exports, "alpha", {
  enumerable: true,
  get: function () {
    return _system.alpha;
  }
});
Object.defineProperty(exports, "createColorScheme", {
  enumerable: true,
  get: function () {
    return _createColorScheme.default;
  }
});
Object.defineProperty(exports, "createMuiTheme", {
  enumerable: true,
  get: function () {
    return _createTheme.createMuiTheme;
  }
});
Object.defineProperty(exports, "createStyles", {
  enumerable: true,
  get: function () {
    return _createStyles.default;
  }
});
Object.defineProperty(exports, "createTheme", {
  enumerable: true,
  get: function () {
    return _createTheme.default;
  }
});
Object.defineProperty(exports, "createTransitions", {
  enumerable: true,
  get: function () {
    return _createTransitions.default;
  }
});
Object.defineProperty(exports, "css", {
  enumerable: true,
  get: function () {
    return _system.css;
  }
});
Object.defineProperty(exports, "darken", {
  enumerable: true,
  get: function () {
    return _system.darken;
  }
});
Object.defineProperty(exports, "decomposeColor", {
  enumerable: true,
  get: function () {
    return _system.decomposeColor;
  }
});
Object.defineProperty(exports, "duration", {
  enumerable: true,
  get: function () {
    return _createTransitions.duration;
  }
});
Object.defineProperty(exports, "easing", {
  enumerable: true,
  get: function () {
    return _createTransitions.easing;
  }
});
Object.defineProperty(exports, "emphasize", {
  enumerable: true,
  get: function () {
    return _system.emphasize;
  }
});
Object.defineProperty(exports, "experimentalStyled", {
  enumerable: true,
  get: function () {
    return _styled.default;
  }
});
Object.defineProperty(exports, "experimental_extendTheme", {
  enumerable: true,
  get: function () {
    return _experimental_extendTheme.default;
  }
});
exports.experimental_sx = experimental_sx;
Object.defineProperty(exports, "extendTheme", {
  enumerable: true,
  get: function () {
    return _createThemeWithVars.default;
  }
});
Object.defineProperty(exports, "getContrastRatio", {
  enumerable: true,
  get: function () {
    return _system.getContrastRatio;
  }
});
Object.defineProperty(exports, "getLuminance", {
  enumerable: true,
  get: function () {
    return _system.getLuminance;
  }
});
Object.defineProperty(exports, "getOverlayAlpha", {
  enumerable: true,
  get: function () {
    return _getOverlayAlpha.default;
  }
});
Object.defineProperty(exports, "hexToRgb", {
  enumerable: true,
  get: function () {
    return _system.hexToRgb;
  }
});
Object.defineProperty(exports, "hslToRgb", {
  enumerable: true,
  get: function () {
    return _system.hslToRgb;
  }
});
Object.defineProperty(exports, "keyframes", {
  enumerable: true,
  get: function () {
    return _system.keyframes;
  }
});
Object.defineProperty(exports, "lighten", {
  enumerable: true,
  get: function () {
    return _system.lighten;
  }
});
Object.defineProperty(exports, "makeStyles", {
  enumerable: true,
  get: function () {
    return _makeStyles.default;
  }
});
Object.defineProperty(exports, "private_createMixins", {
  enumerable: true,
  get: function () {
    return _createMixins.default;
  }
});
Object.defineProperty(exports, "private_createTypography", {
  enumerable: true,
  get: function () {
    return _createTypography.default;
  }
});
Object.defineProperty(exports, "private_excludeVariablesFromRoot", {
  enumerable: true,
  get: function () {
    return _excludeVariablesFromRoot.default;
  }
});
Object.defineProperty(exports, "recomposeColor", {
  enumerable: true,
  get: function () {
    return _system.recomposeColor;
  }
});
Object.defineProperty(exports, "responsiveFontSizes", {
  enumerable: true,
  get: function () {
    return _responsiveFontSizes.default;
  }
});
Object.defineProperty(exports, "rgbToHex", {
  enumerable: true,
  get: function () {
    return _system.rgbToHex;
  }
});
Object.defineProperty(exports, "shouldSkipGeneratingVar", {
  enumerable: true,
  get: function () {
    return _shouldSkipGeneratingVar.default;
  }
});
Object.defineProperty(exports, "styled", {
  enumerable: true,
  get: function () {
    return _styled.default;
  }
});
Object.defineProperty(exports, "unstable_createBreakpoints", {
  enumerable: true,
  get: function () {
    return _createBreakpoints.unstable_createBreakpoints;
  }
});
Object.defineProperty(exports, "unstable_createMuiStrictModeTheme", {
  enumerable: true,
  get: function () {
    return _createMuiStrictModeTheme.default;
  }
});
Object.defineProperty(exports, "unstable_getUnit", {
  enumerable: true,
  get: function () {
    return _cssUtils.getUnit;
  }
});
Object.defineProperty(exports, "unstable_toUnitless", {
  enumerable: true,
  get: function () {
    return _cssUtils.toUnitless;
  }
});
Object.defineProperty(exports, "useTheme", {
  enumerable: true,
  get: function () {
    return _useTheme.default;
  }
});
Object.defineProperty(exports, "useThemeProps", {
  enumerable: true,
  get: function () {
    return _useThemeProps.default;
  }
});
Object.defineProperty(exports, "withStyles", {
  enumerable: true,
  get: function () {
    return _withStyles.default;
  }
});
Object.defineProperty(exports, "withTheme", {
  enumerable: true,
  get: function () {
    return _withTheme.default;
  }
});
var _formatMuiErrorMessage2 = _interopRequireDefault(require("@mui/utils/formatMuiErrorMessage"));
var _identifier = _interopRequireDefault(require("./identifier"));
var _adaptV4Theme = _interopRequireDefault(require("./adaptV4Theme"));
var _system = require("@mui/system");
var _createBreakpoints = require("@mui/system/createBreakpoints");
var _createTheme = _interopRequireWildcard(require("./createTheme"));
var _createMuiStrictModeTheme = _interopRequireDefault(require("./createMuiStrictModeTheme"));
var _createStyles = _interopRequireDefault(require("./createStyles"));
var _cssUtils = require("./cssUtils");
var _responsiveFontSizes = _interopRequireDefault(require("./responsiveFontSizes"));
var _createTransitions = _interopRequireWildcard(require("./createTransitions"));
var _createColorScheme = _interopRequireDefault(require("./createColorScheme"));
var _useTheme = _interopRequireDefault(require("./useTheme"));
var _useThemeProps = _interopRequireDefault(require("./useThemeProps"));
var _styled = _interopRequireDefault(require("./styled"));
var _ThemeProvider = _interopRequireDefault(require("./ThemeProvider"));
var _makeStyles = _interopRequireDefault(require("./makeStyles"));
var _withStyles = _interopRequireDefault(require("./withStyles"));
var _withTheme = _interopRequireDefault(require("./withTheme"));
var _ThemeProviderWithVars = require("./ThemeProviderWithVars");
Object.keys(_ThemeProviderWithVars).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _ThemeProviderWithVars[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _ThemeProviderWithVars[key];
    }
  });
});
var _createThemeWithVars = _interopRequireDefault(require("./createThemeWithVars"));
var _experimental_extendTheme = _interopRequireDefault(require("./experimental_extendTheme"));
var _getOverlayAlpha = _interopRequireDefault(require("./getOverlayAlpha"));
var _shouldSkipGeneratingVar = _interopRequireDefault(require("./shouldSkipGeneratingVar"));
var _createTypography = _interopRequireDefault(require("./createTypography"));
var _createMixins = _interopRequireDefault(require("./createMixins"));
var _excludeVariablesFromRoot = _interopRequireDefault(require("./excludeVariablesFromRoot"));
// TODO: Remove this function in v6.
// eslint-disable-next-line @typescript-eslint/naming-convention
function experimental_sx() {
  throw new Error(process.env.NODE_ENV !== "production" ? 'MUI: The `experimental_sx` has been moved to `theme.unstable_sx`.' + 'For more details, see https://github.com/mui/material-ui/pull/35150.' : (0, _formatMuiErrorMessage2.default)(19));
}

// The legacy utilities from @mui/styles
// These are just empty functions that throws when invoked

// TODO: Remove in v7

// Private methods for creating parts of the theme