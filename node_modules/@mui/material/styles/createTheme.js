import createPalette from "./createPalette.js";
import createThemeWithVars from "./createThemeWithVars.js";
import createThemeNoVars from "./createThemeNoVars.js";
export { createMuiTheme } from "./createThemeNoVars.js";
// eslint-disable-next-line consistent-return
function attachColorScheme(theme, scheme, colorScheme) {
  if (!theme.colorSchemes) {
    return undefined;
  }
  if (colorScheme) {
    theme.colorSchemes[scheme] = {
      ...(colorScheme !== true && colorScheme),
      palette: createPalette({
        ...(colorScheme === true ? {} : colorScheme.palette),
        mode: scheme
      }) // cast type to skip module augmentation test
    };
  }
}

/**
 * Generate a theme base on the options received.
 * @param options Takes an incomplete theme object and adds the missing parts.
 * @param args Deep merge the arguments with the about to be returned theme.
 * @returns A complete, ready-to-use theme object.
 */
export default function createTheme(options = {},
// cast type to skip module augmentation test
...args) {
  const {
    palette,
    cssVariables = false,
    colorSchemes: initialColorSchemes = !palette ? {
      light: true
    } : undefined,
    defaultColorScheme: initialDefaultColorScheme = palette?.mode,
    ...rest
  } = options;
  const defaultColorSchemeInput = initialDefaultColorScheme || 'light';
  const defaultScheme = initialColorSchemes?.[defaultColorSchemeInput];
  const colorSchemesInput = {
    ...initialColorSchemes,
    ...(palette ? {
      [defaultColorSchemeInput]: {
        ...(typeof defaultScheme !== 'boolean' && defaultScheme),
        palette
      }
    } : undefined)
  };
  if (cssVariables === false) {
    if (!('colorSchemes' in options)) {
      // Behaves exactly as v5
      return createThemeNoVars(options, ...args);
    }
    let paletteOptions = palette;
    if (!('palette' in options)) {
      if (colorSchemesInput[defaultColorSchemeInput]) {
        if (colorSchemesInput[defaultColorSchemeInput] !== true) {
          paletteOptions = colorSchemesInput[defaultColorSchemeInput].palette;
        } else if (defaultColorSchemeInput === 'dark') {
          // @ts-ignore to prevent the module augmentation test from failing
          paletteOptions = {
            mode: 'dark'
          };
        }
      }
    }
    const theme = createThemeNoVars({
      ...options,
      palette: paletteOptions
    }, ...args);
    theme.defaultColorScheme = defaultColorSchemeInput;
    theme.colorSchemes = colorSchemesInput;
    if (theme.palette.mode === 'light') {
      theme.colorSchemes.light = {
        ...(colorSchemesInput.light !== true && colorSchemesInput.light),
        palette: theme.palette
      };
      attachColorScheme(theme, 'dark', colorSchemesInput.dark);
    }
    if (theme.palette.mode === 'dark') {
      theme.colorSchemes.dark = {
        ...(colorSchemesInput.dark !== true && colorSchemesInput.dark),
        palette: theme.palette
      };
      attachColorScheme(theme, 'light', colorSchemesInput.light);
    }
    return theme;
  }
  if (!palette && !('light' in colorSchemesInput) && defaultColorSchemeInput === 'light') {
    colorSchemesInput.light = true;
  }
  return createThemeWithVars({
    ...rest,
    colorSchemes: colorSchemesInput,
    defaultColorScheme: defaultColorSchemeInput,
    ...(typeof cssVariables !== 'boolean' && cssVariables)
  }, ...args);
}