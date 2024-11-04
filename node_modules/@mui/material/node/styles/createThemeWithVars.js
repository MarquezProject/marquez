"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createGetCssVar = void 0;
exports.default = createThemeWithVars;
var _formatMuiErrorMessage2 = _interopRequireDefault(require("@mui/utils/formatMuiErrorMessage"));
var _deepmerge = _interopRequireDefault(require("@mui/utils/deepmerge"));
var _system = require("@mui/system");
var _spacing = require("@mui/system/spacing");
var _cssVars = require("@mui/system/cssVars");
var _styleFunctionSx = _interopRequireWildcard(require("@mui/system/styleFunctionSx"));
var _colorManipulator = require("@mui/system/colorManipulator");
var _createThemeNoVars = _interopRequireDefault(require("./createThemeNoVars"));
var _createColorScheme = _interopRequireWildcard(require("./createColorScheme"));
var _shouldSkipGeneratingVar = _interopRequireDefault(require("./shouldSkipGeneratingVar"));
var _createGetSelector = _interopRequireDefault(require("./createGetSelector"));
var _stringifyTheme = require("./stringifyTheme");
function assignNode(obj, keys) {
  keys.forEach(k => {
    if (!obj[k]) {
      obj[k] = {};
    }
  });
}
function setColor(obj, key, defaultValue) {
  if (!obj[key] && defaultValue) {
    obj[key] = defaultValue;
  }
}
function toRgb(color) {
  if (!color || !color.startsWith('hsl')) {
    return color;
  }
  return (0, _colorManipulator.hslToRgb)(color);
}
function setColorChannel(obj, key) {
  if (!(`${key}Channel` in obj)) {
    // custom channel token is not provided, generate one.
    // if channel token can't be generated, show a warning.
    obj[`${key}Channel`] = (0, _colorManipulator.private_safeColorChannel)(toRgb(obj[key]), `MUI: Can't create \`palette.${key}Channel\` because \`palette.${key}\` is not one of these formats: #nnn, #nnnnnn, rgb(), rgba(), hsl(), hsla(), color().` + '\n' + `To suppress this warning, you need to explicitly provide the \`palette.${key}Channel\` as a string (in rgb format, for example "12 12 12") or undefined if you want to remove the channel token.`);
  }
}
function getSpacingVal(spacingInput) {
  if (typeof spacingInput === 'number') {
    return `${spacingInput}px`;
  }
  if (typeof spacingInput === 'string' || typeof spacingInput === 'function' || Array.isArray(spacingInput)) {
    return spacingInput;
  }
  return '8px';
}
const silent = fn => {
  try {
    return fn();
  } catch (error) {
    // ignore error
  }
  return undefined;
};
const createGetCssVar = (cssVarPrefix = 'mui') => (0, _system.unstable_createGetCssVar)(cssVarPrefix);
exports.createGetCssVar = createGetCssVar;
function attachColorScheme(colorSchemes, scheme, restTheme, colorScheme) {
  if (!scheme) {
    return undefined;
  }
  scheme = scheme === true ? {} : scheme;
  const mode = colorScheme === 'dark' ? 'dark' : 'light';
  if (!restTheme) {
    colorSchemes[colorScheme] = (0, _createColorScheme.default)({
      ...scheme,
      palette: {
        mode,
        ...scheme?.palette
      }
    });
    return undefined;
  }
  const {
    palette,
    ...muiTheme
  } = (0, _createThemeNoVars.default)({
    ...restTheme,
    palette: {
      mode,
      ...scheme?.palette
    }
  });
  colorSchemes[colorScheme] = {
    ...scheme,
    palette,
    opacity: {
      ...(0, _createColorScheme.getOpacity)(mode),
      ...scheme?.opacity
    },
    overlays: scheme?.overlays || (0, _createColorScheme.getOverlays)(mode)
  };
  return muiTheme;
}

/**
 * A default `createThemeWithVars` comes with a single color scheme, either `light` or `dark` based on the `defaultColorScheme`.
 * This is better suited for apps that only need a single color scheme.
 *
 * To enable built-in `light` and `dark` color schemes, either:
 * 1. provide a `colorSchemeSelector` to define how the color schemes will change.
 * 2. provide `colorSchemes.dark` will set `colorSchemeSelector: 'media'` by default.
 */
function createThemeWithVars(options = {}, ...args) {
  const {
    colorSchemes: colorSchemesInput = {
      light: true
    },
    defaultColorScheme: defaultColorSchemeInput,
    disableCssColorScheme = false,
    cssVarPrefix = 'mui',
    shouldSkipGeneratingVar = _shouldSkipGeneratingVar.default,
    colorSchemeSelector: selector = colorSchemesInput.light && colorSchemesInput.dark ? 'media' : undefined,
    rootSelector = ':root',
    ...input
  } = options;
  const firstColorScheme = Object.keys(colorSchemesInput)[0];
  const defaultColorScheme = defaultColorSchemeInput || (colorSchemesInput.light && firstColorScheme !== 'light' ? 'light' : firstColorScheme);
  const getCssVar = createGetCssVar(cssVarPrefix);
  const {
    [defaultColorScheme]: defaultSchemeInput,
    light: builtInLight,
    dark: builtInDark,
    ...customColorSchemes
  } = colorSchemesInput;
  const colorSchemes = {
    ...customColorSchemes
  };
  let defaultScheme = defaultSchemeInput;

  // For built-in light and dark color schemes, ensure that the value is valid if they are the default color scheme.
  if (defaultColorScheme === 'dark' && !('dark' in colorSchemesInput) || defaultColorScheme === 'light' && !('light' in colorSchemesInput)) {
    defaultScheme = true;
  }
  if (!defaultScheme) {
    throw new Error(process.env.NODE_ENV !== "production" ? `MUI: The \`colorSchemes.${defaultColorScheme}\` option is either missing or invalid.` : (0, _formatMuiErrorMessage2.default)(21, defaultColorScheme));
  }

  // Create the palette for the default color scheme, either `light`, `dark`, or custom color scheme.
  const muiTheme = attachColorScheme(colorSchemes, defaultScheme, input, defaultColorScheme);
  if (builtInLight && !colorSchemes.light) {
    attachColorScheme(colorSchemes, builtInLight, undefined, 'light');
  }
  if (builtInDark && !colorSchemes.dark) {
    attachColorScheme(colorSchemes, builtInDark, undefined, 'dark');
  }
  let theme = {
    defaultColorScheme,
    ...muiTheme,
    cssVarPrefix,
    colorSchemeSelector: selector,
    rootSelector,
    getCssVar,
    colorSchemes,
    font: {
      ...(0, _cssVars.prepareTypographyVars)(muiTheme.typography),
      ...muiTheme.font
    },
    spacing: getSpacingVal(input.spacing)
  };
  Object.keys(theme.colorSchemes).forEach(key => {
    const palette = theme.colorSchemes[key].palette;
    const setCssVarColor = cssVar => {
      const tokens = cssVar.split('-');
      const color = tokens[1];
      const colorToken = tokens[2];
      return getCssVar(cssVar, palette[color][colorToken]);
    };

    // attach black & white channels to common node
    if (palette.mode === 'light') {
      setColor(palette.common, 'background', '#fff');
      setColor(palette.common, 'onBackground', '#000');
    }
    if (palette.mode === 'dark') {
      setColor(palette.common, 'background', '#000');
      setColor(palette.common, 'onBackground', '#fff');
    }

    // assign component variables
    assignNode(palette, ['Alert', 'AppBar', 'Avatar', 'Button', 'Chip', 'FilledInput', 'LinearProgress', 'Skeleton', 'Slider', 'SnackbarContent', 'SpeedDialAction', 'StepConnector', 'StepContent', 'Switch', 'TableCell', 'Tooltip']);
    if (palette.mode === 'light') {
      setColor(palette.Alert, 'errorColor', (0, _colorManipulator.private_safeDarken)(palette.error.light, 0.6));
      setColor(palette.Alert, 'infoColor', (0, _colorManipulator.private_safeDarken)(palette.info.light, 0.6));
      setColor(palette.Alert, 'successColor', (0, _colorManipulator.private_safeDarken)(palette.success.light, 0.6));
      setColor(palette.Alert, 'warningColor', (0, _colorManipulator.private_safeDarken)(palette.warning.light, 0.6));
      setColor(palette.Alert, 'errorFilledBg', setCssVarColor('palette-error-main'));
      setColor(palette.Alert, 'infoFilledBg', setCssVarColor('palette-info-main'));
      setColor(palette.Alert, 'successFilledBg', setCssVarColor('palette-success-main'));
      setColor(palette.Alert, 'warningFilledBg', setCssVarColor('palette-warning-main'));
      setColor(palette.Alert, 'errorFilledColor', silent(() => palette.getContrastText(palette.error.main)));
      setColor(palette.Alert, 'infoFilledColor', silent(() => palette.getContrastText(palette.info.main)));
      setColor(palette.Alert, 'successFilledColor', silent(() => palette.getContrastText(palette.success.main)));
      setColor(palette.Alert, 'warningFilledColor', silent(() => palette.getContrastText(palette.warning.main)));
      setColor(palette.Alert, 'errorStandardBg', (0, _colorManipulator.private_safeLighten)(palette.error.light, 0.9));
      setColor(palette.Alert, 'infoStandardBg', (0, _colorManipulator.private_safeLighten)(palette.info.light, 0.9));
      setColor(palette.Alert, 'successStandardBg', (0, _colorManipulator.private_safeLighten)(palette.success.light, 0.9));
      setColor(palette.Alert, 'warningStandardBg', (0, _colorManipulator.private_safeLighten)(palette.warning.light, 0.9));
      setColor(palette.Alert, 'errorIconColor', setCssVarColor('palette-error-main'));
      setColor(palette.Alert, 'infoIconColor', setCssVarColor('palette-info-main'));
      setColor(palette.Alert, 'successIconColor', setCssVarColor('palette-success-main'));
      setColor(palette.Alert, 'warningIconColor', setCssVarColor('palette-warning-main'));
      setColor(palette.AppBar, 'defaultBg', setCssVarColor('palette-grey-100'));
      setColor(palette.Avatar, 'defaultBg', setCssVarColor('palette-grey-400'));
      setColor(palette.Button, 'inheritContainedBg', setCssVarColor('palette-grey-300'));
      setColor(palette.Button, 'inheritContainedHoverBg', setCssVarColor('palette-grey-A100'));
      setColor(palette.Chip, 'defaultBorder', setCssVarColor('palette-grey-400'));
      setColor(palette.Chip, 'defaultAvatarColor', setCssVarColor('palette-grey-700'));
      setColor(palette.Chip, 'defaultIconColor', setCssVarColor('palette-grey-700'));
      setColor(palette.FilledInput, 'bg', 'rgba(0, 0, 0, 0.06)');
      setColor(palette.FilledInput, 'hoverBg', 'rgba(0, 0, 0, 0.09)');
      setColor(palette.FilledInput, 'disabledBg', 'rgba(0, 0, 0, 0.12)');
      setColor(palette.LinearProgress, 'primaryBg', (0, _colorManipulator.private_safeLighten)(palette.primary.main, 0.62));
      setColor(palette.LinearProgress, 'secondaryBg', (0, _colorManipulator.private_safeLighten)(palette.secondary.main, 0.62));
      setColor(palette.LinearProgress, 'errorBg', (0, _colorManipulator.private_safeLighten)(palette.error.main, 0.62));
      setColor(palette.LinearProgress, 'infoBg', (0, _colorManipulator.private_safeLighten)(palette.info.main, 0.62));
      setColor(palette.LinearProgress, 'successBg', (0, _colorManipulator.private_safeLighten)(palette.success.main, 0.62));
      setColor(palette.LinearProgress, 'warningBg', (0, _colorManipulator.private_safeLighten)(palette.warning.main, 0.62));
      setColor(palette.Skeleton, 'bg', `rgba(${setCssVarColor('palette-text-primaryChannel')} / 0.11)`);
      setColor(palette.Slider, 'primaryTrack', (0, _colorManipulator.private_safeLighten)(palette.primary.main, 0.62));
      setColor(palette.Slider, 'secondaryTrack', (0, _colorManipulator.private_safeLighten)(palette.secondary.main, 0.62));
      setColor(palette.Slider, 'errorTrack', (0, _colorManipulator.private_safeLighten)(palette.error.main, 0.62));
      setColor(palette.Slider, 'infoTrack', (0, _colorManipulator.private_safeLighten)(palette.info.main, 0.62));
      setColor(palette.Slider, 'successTrack', (0, _colorManipulator.private_safeLighten)(palette.success.main, 0.62));
      setColor(palette.Slider, 'warningTrack', (0, _colorManipulator.private_safeLighten)(palette.warning.main, 0.62));
      const snackbarContentBackground = (0, _colorManipulator.private_safeEmphasize)(palette.background.default, 0.8);
      setColor(palette.SnackbarContent, 'bg', snackbarContentBackground);
      setColor(palette.SnackbarContent, 'color', silent(() => palette.getContrastText(snackbarContentBackground)));
      setColor(palette.SpeedDialAction, 'fabHoverBg', (0, _colorManipulator.private_safeEmphasize)(palette.background.paper, 0.15));
      setColor(palette.StepConnector, 'border', setCssVarColor('palette-grey-400'));
      setColor(palette.StepContent, 'border', setCssVarColor('palette-grey-400'));
      setColor(palette.Switch, 'defaultColor', setCssVarColor('palette-common-white'));
      setColor(palette.Switch, 'defaultDisabledColor', setCssVarColor('palette-grey-100'));
      setColor(palette.Switch, 'primaryDisabledColor', (0, _colorManipulator.private_safeLighten)(palette.primary.main, 0.62));
      setColor(palette.Switch, 'secondaryDisabledColor', (0, _colorManipulator.private_safeLighten)(palette.secondary.main, 0.62));
      setColor(palette.Switch, 'errorDisabledColor', (0, _colorManipulator.private_safeLighten)(palette.error.main, 0.62));
      setColor(palette.Switch, 'infoDisabledColor', (0, _colorManipulator.private_safeLighten)(palette.info.main, 0.62));
      setColor(palette.Switch, 'successDisabledColor', (0, _colorManipulator.private_safeLighten)(palette.success.main, 0.62));
      setColor(palette.Switch, 'warningDisabledColor', (0, _colorManipulator.private_safeLighten)(palette.warning.main, 0.62));
      setColor(palette.TableCell, 'border', (0, _colorManipulator.private_safeLighten)((0, _colorManipulator.private_safeAlpha)(palette.divider, 1), 0.88));
      setColor(palette.Tooltip, 'bg', (0, _colorManipulator.private_safeAlpha)(palette.grey[700], 0.92));
    }
    if (palette.mode === 'dark') {
      setColor(palette.Alert, 'errorColor', (0, _colorManipulator.private_safeLighten)(palette.error.light, 0.6));
      setColor(palette.Alert, 'infoColor', (0, _colorManipulator.private_safeLighten)(palette.info.light, 0.6));
      setColor(palette.Alert, 'successColor', (0, _colorManipulator.private_safeLighten)(palette.success.light, 0.6));
      setColor(palette.Alert, 'warningColor', (0, _colorManipulator.private_safeLighten)(palette.warning.light, 0.6));
      setColor(palette.Alert, 'errorFilledBg', setCssVarColor('palette-error-dark'));
      setColor(palette.Alert, 'infoFilledBg', setCssVarColor('palette-info-dark'));
      setColor(palette.Alert, 'successFilledBg', setCssVarColor('palette-success-dark'));
      setColor(palette.Alert, 'warningFilledBg', setCssVarColor('palette-warning-dark'));
      setColor(palette.Alert, 'errorFilledColor', silent(() => palette.getContrastText(palette.error.dark)));
      setColor(palette.Alert, 'infoFilledColor', silent(() => palette.getContrastText(palette.info.dark)));
      setColor(palette.Alert, 'successFilledColor', silent(() => palette.getContrastText(palette.success.dark)));
      setColor(palette.Alert, 'warningFilledColor', silent(() => palette.getContrastText(palette.warning.dark)));
      setColor(palette.Alert, 'errorStandardBg', (0, _colorManipulator.private_safeDarken)(palette.error.light, 0.9));
      setColor(palette.Alert, 'infoStandardBg', (0, _colorManipulator.private_safeDarken)(palette.info.light, 0.9));
      setColor(palette.Alert, 'successStandardBg', (0, _colorManipulator.private_safeDarken)(palette.success.light, 0.9));
      setColor(palette.Alert, 'warningStandardBg', (0, _colorManipulator.private_safeDarken)(palette.warning.light, 0.9));
      setColor(palette.Alert, 'errorIconColor', setCssVarColor('palette-error-main'));
      setColor(palette.Alert, 'infoIconColor', setCssVarColor('palette-info-main'));
      setColor(palette.Alert, 'successIconColor', setCssVarColor('palette-success-main'));
      setColor(palette.Alert, 'warningIconColor', setCssVarColor('palette-warning-main'));
      setColor(palette.AppBar, 'defaultBg', setCssVarColor('palette-grey-900'));
      setColor(palette.AppBar, 'darkBg', setCssVarColor('palette-background-paper')); // specific for dark mode
      setColor(palette.AppBar, 'darkColor', setCssVarColor('palette-text-primary')); // specific for dark mode
      setColor(palette.Avatar, 'defaultBg', setCssVarColor('palette-grey-600'));
      setColor(palette.Button, 'inheritContainedBg', setCssVarColor('palette-grey-800'));
      setColor(palette.Button, 'inheritContainedHoverBg', setCssVarColor('palette-grey-700'));
      setColor(palette.Chip, 'defaultBorder', setCssVarColor('palette-grey-700'));
      setColor(palette.Chip, 'defaultAvatarColor', setCssVarColor('palette-grey-300'));
      setColor(palette.Chip, 'defaultIconColor', setCssVarColor('palette-grey-300'));
      setColor(palette.FilledInput, 'bg', 'rgba(255, 255, 255, 0.09)');
      setColor(palette.FilledInput, 'hoverBg', 'rgba(255, 255, 255, 0.13)');
      setColor(palette.FilledInput, 'disabledBg', 'rgba(255, 255, 255, 0.12)');
      setColor(palette.LinearProgress, 'primaryBg', (0, _colorManipulator.private_safeDarken)(palette.primary.main, 0.5));
      setColor(palette.LinearProgress, 'secondaryBg', (0, _colorManipulator.private_safeDarken)(palette.secondary.main, 0.5));
      setColor(palette.LinearProgress, 'errorBg', (0, _colorManipulator.private_safeDarken)(palette.error.main, 0.5));
      setColor(palette.LinearProgress, 'infoBg', (0, _colorManipulator.private_safeDarken)(palette.info.main, 0.5));
      setColor(palette.LinearProgress, 'successBg', (0, _colorManipulator.private_safeDarken)(palette.success.main, 0.5));
      setColor(palette.LinearProgress, 'warningBg', (0, _colorManipulator.private_safeDarken)(palette.warning.main, 0.5));
      setColor(palette.Skeleton, 'bg', `rgba(${setCssVarColor('palette-text-primaryChannel')} / 0.13)`);
      setColor(palette.Slider, 'primaryTrack', (0, _colorManipulator.private_safeDarken)(palette.primary.main, 0.5));
      setColor(palette.Slider, 'secondaryTrack', (0, _colorManipulator.private_safeDarken)(palette.secondary.main, 0.5));
      setColor(palette.Slider, 'errorTrack', (0, _colorManipulator.private_safeDarken)(palette.error.main, 0.5));
      setColor(palette.Slider, 'infoTrack', (0, _colorManipulator.private_safeDarken)(palette.info.main, 0.5));
      setColor(palette.Slider, 'successTrack', (0, _colorManipulator.private_safeDarken)(palette.success.main, 0.5));
      setColor(palette.Slider, 'warningTrack', (0, _colorManipulator.private_safeDarken)(palette.warning.main, 0.5));
      const snackbarContentBackground = (0, _colorManipulator.private_safeEmphasize)(palette.background.default, 0.98);
      setColor(palette.SnackbarContent, 'bg', snackbarContentBackground);
      setColor(palette.SnackbarContent, 'color', silent(() => palette.getContrastText(snackbarContentBackground)));
      setColor(palette.SpeedDialAction, 'fabHoverBg', (0, _colorManipulator.private_safeEmphasize)(palette.background.paper, 0.15));
      setColor(palette.StepConnector, 'border', setCssVarColor('palette-grey-600'));
      setColor(palette.StepContent, 'border', setCssVarColor('palette-grey-600'));
      setColor(palette.Switch, 'defaultColor', setCssVarColor('palette-grey-300'));
      setColor(palette.Switch, 'defaultDisabledColor', setCssVarColor('palette-grey-600'));
      setColor(palette.Switch, 'primaryDisabledColor', (0, _colorManipulator.private_safeDarken)(palette.primary.main, 0.55));
      setColor(palette.Switch, 'secondaryDisabledColor', (0, _colorManipulator.private_safeDarken)(palette.secondary.main, 0.55));
      setColor(palette.Switch, 'errorDisabledColor', (0, _colorManipulator.private_safeDarken)(palette.error.main, 0.55));
      setColor(palette.Switch, 'infoDisabledColor', (0, _colorManipulator.private_safeDarken)(palette.info.main, 0.55));
      setColor(palette.Switch, 'successDisabledColor', (0, _colorManipulator.private_safeDarken)(palette.success.main, 0.55));
      setColor(palette.Switch, 'warningDisabledColor', (0, _colorManipulator.private_safeDarken)(palette.warning.main, 0.55));
      setColor(palette.TableCell, 'border', (0, _colorManipulator.private_safeDarken)((0, _colorManipulator.private_safeAlpha)(palette.divider, 1), 0.68));
      setColor(palette.Tooltip, 'bg', (0, _colorManipulator.private_safeAlpha)(palette.grey[700], 0.92));
    }

    // MUI X - DataGrid needs this token.
    setColorChannel(palette.background, 'default');

    // added for consistency with the `background.default` token
    setColorChannel(palette.background, 'paper');
    setColorChannel(palette.common, 'background');
    setColorChannel(palette.common, 'onBackground');
    setColorChannel(palette, 'divider');
    Object.keys(palette).forEach(color => {
      const colors = palette[color];

      // The default palettes (primary, secondary, error, info, success, and warning) errors are handled by the above `createTheme(...)`.

      if (colors && typeof colors === 'object') {
        // Silent the error for custom palettes.
        if (colors.main) {
          setColor(palette[color], 'mainChannel', (0, _colorManipulator.private_safeColorChannel)(toRgb(colors.main)));
        }
        if (colors.light) {
          setColor(palette[color], 'lightChannel', (0, _colorManipulator.private_safeColorChannel)(toRgb(colors.light)));
        }
        if (colors.dark) {
          setColor(palette[color], 'darkChannel', (0, _colorManipulator.private_safeColorChannel)(toRgb(colors.dark)));
        }
        if (colors.contrastText) {
          setColor(palette[color], 'contrastTextChannel', (0, _colorManipulator.private_safeColorChannel)(toRgb(colors.contrastText)));
        }
        if (color === 'text') {
          // Text colors: text.primary, text.secondary
          setColorChannel(palette[color], 'primary');
          setColorChannel(palette[color], 'secondary');
        }
        if (color === 'action') {
          // Action colors: action.active, action.selected
          if (colors.active) {
            setColorChannel(palette[color], 'active');
          }
          if (colors.selected) {
            setColorChannel(palette[color], 'selected');
          }
        }
      }
    });
  });
  theme = args.reduce((acc, argument) => (0, _deepmerge.default)(acc, argument), theme);
  const parserConfig = {
    prefix: cssVarPrefix,
    disableCssColorScheme,
    shouldSkipGeneratingVar,
    getSelector: (0, _createGetSelector.default)(theme)
  };
  const {
    vars,
    generateThemeVars,
    generateStyleSheets
  } = (0, _cssVars.prepareCssVars)(theme, parserConfig);
  theme.vars = vars;
  Object.entries(theme.colorSchemes[theme.defaultColorScheme]).forEach(([key, value]) => {
    theme[key] = value;
  });
  theme.generateThemeVars = generateThemeVars;
  theme.generateStyleSheets = generateStyleSheets;
  theme.generateSpacing = function generateSpacing() {
    return (0, _system.createSpacing)(input.spacing, (0, _spacing.createUnarySpacing)(this));
  };
  theme.getColorSchemeSelector = (0, _cssVars.createGetColorSchemeSelector)(selector);
  theme.spacing = theme.generateSpacing();
  theme.shouldSkipGeneratingVar = shouldSkipGeneratingVar;
  theme.unstable_sxConfig = {
    ..._styleFunctionSx.unstable_defaultSxConfig,
    ...input?.unstable_sxConfig
  };
  theme.unstable_sx = function sx(props) {
    return (0, _styleFunctionSx.default)({
      sx: props,
      theme: this
    });
  };
  theme.toRuntimeSource = _stringifyTheme.stringifyTheme; // for Pigment CSS integration

  return theme;
}