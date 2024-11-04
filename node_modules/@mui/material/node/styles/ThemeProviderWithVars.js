"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.CssVarsProvider = void 0;
exports.Experimental_CssVarsProvider = Experimental_CssVarsProvider;
exports.useColorScheme = exports.getInitColorSchemeScript = void 0;
var React = _interopRequireWildcard(require("react"));
var _styleFunctionSx = _interopRequireDefault(require("@mui/system/styleFunctionSx"));
var _system = require("@mui/system");
var _createTheme = _interopRequireDefault(require("./createTheme"));
var _createTypography = _interopRequireDefault(require("./createTypography"));
var _identifier = _interopRequireDefault(require("./identifier"));
var _InitColorSchemeScript = require("../InitColorSchemeScript/InitColorSchemeScript");
var _jsxRuntime = require("react/jsx-runtime");
const {
  CssVarsProvider: InternalCssVarsProvider,
  useColorScheme,
  getInitColorSchemeScript: deprecatedGetInitColorSchemeScript
} = (0, _system.unstable_createCssVarsProvider)({
  themeId: _identifier.default,
  // @ts-ignore ignore module augmentation tests
  theme: () => (0, _createTheme.default)({
    cssVariables: true
  }),
  colorSchemeStorageKey: _InitColorSchemeScript.defaultConfig.colorSchemeStorageKey,
  modeStorageKey: _InitColorSchemeScript.defaultConfig.modeStorageKey,
  defaultColorScheme: {
    light: _InitColorSchemeScript.defaultConfig.defaultLightColorScheme,
    dark: _InitColorSchemeScript.defaultConfig.defaultDarkColorScheme
  },
  resolveTheme: theme => {
    const newTheme = {
      ...theme,
      typography: (0, _createTypography.default)(theme.palette, theme.typography)
    };
    newTheme.unstable_sx = function sx(props) {
      return (0, _styleFunctionSx.default)({
        sx: props,
        theme: this
      });
    };
    return newTheme;
  }
});
exports.useColorScheme = useColorScheme;
let warnedOnce = false;

// TODO: remove in v7
// eslint-disable-next-line @typescript-eslint/naming-convention
function Experimental_CssVarsProvider(props) {
  if (process.env.NODE_ENV !== 'production') {
    if (!warnedOnce) {
      console.warn(['MUI: The Experimental_CssVarsProvider component has been ported into ThemeProvider.', '', "You should use `import { ThemeProvider } from '@mui/material/styles'` instead.", 'For more details, check out https://mui.com/material-ui/customization/css-theme-variables/usage/'].join('\n'));
      warnedOnce = true;
    }
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(InternalCssVarsProvider, {
    ...props
  });
}
let warnedInitScriptOnce = false;

// TODO: remove in v7
const getInitColorSchemeScript = params => {
  if (!warnedInitScriptOnce) {
    console.warn(['MUI: The getInitColorSchemeScript function has been deprecated.', '', "You should use `import InitColorSchemeScript from '@mui/material/InitColorSchemeScript'`", 'and replace the function call with `<InitColorSchemeScript />` instead.'].join('\n'));
    warnedInitScriptOnce = true;
  }
  return deprecatedGetInitColorSchemeScript(params);
};

/**
 * TODO: remove this export in v7
 * @deprecated
 * The `CssVarsProvider` component has been deprecated and ported into `ThemeProvider`.
 *
 * You should use `ThemeProvider` and `createTheme()` instead:
 *
 * ```diff
 * - import { CssVarsProvider, extendTheme } from '@mui/material/styles';
 * + import { ThemeProvider, createTheme } from '@mui/material/styles';
 *
 * - const theme = extendTheme();
 * + const theme = createTheme({
 * +   cssVariables: true,
 * +   colorSchemes: { light: true, dark: true },
 * + });
 *
 * - <CssVarsProvider theme={theme}>
 * + <ThemeProvider theme={theme}>
 * ```
 *
 * To see the full documentation, check out https://mui.com/material-ui/customization/css-theme-variables/usage/.
 */
exports.getInitColorSchemeScript = getInitColorSchemeScript;
const CssVarsProvider = exports.CssVarsProvider = InternalCssVarsProvider;