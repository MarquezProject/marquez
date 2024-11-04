import { CssVarsThemeOptions } from './createThemeWithVars';
import { Theme, ThemeOptions } from './createThemeNoVars';
export { createMuiTheme } from './createThemeNoVars';
export type { ThemeOptions, Theme, CssThemeVariables } from './createThemeNoVars';
/**
 * Generate a theme base on the options received.
 * @param options Takes an incomplete theme object and adds the missing parts.
 * @param args Deep merge the arguments with the about to be returned theme.
 * @returns A complete, ready-to-use theme object.
 */
export default function createTheme(options?: Omit<ThemeOptions, 'components'> & Pick<CssVarsThemeOptions, 'defaultColorScheme' | 'colorSchemes' | 'components'> & {
    cssVariables?: boolean | Pick<CssVarsThemeOptions, 'colorSchemeSelector' | 'rootSelector' | 'disableCssColorScheme' | 'cssVarPrefix' | 'shouldSkipGeneratingVar'>;
}, // cast type to skip module augmentation test
...args: object[]): Theme;
