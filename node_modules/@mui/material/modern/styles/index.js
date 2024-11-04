import _formatMuiErrorMessage from "@mui/utils/formatMuiErrorMessage";
export { default as THEME_ID } from "./identifier.js";
export { default as adaptV4Theme } from "./adaptV4Theme.js";
export { hexToRgb, rgbToHex, hslToRgb, decomposeColor, recomposeColor, getContrastRatio, getLuminance, emphasize, alpha, darken, lighten, css, keyframes } from '@mui/system';
export { unstable_createBreakpoints } from '@mui/system/createBreakpoints';
// TODO: Remove this function in v6.
// eslint-disable-next-line @typescript-eslint/naming-convention
export function experimental_sx() {
  throw new Error(process.env.NODE_ENV !== "production" ? 'MUI: The `experimental_sx` has been moved to `theme.unstable_sx`.' + 'For more details, see https://github.com/mui/material-ui/pull/35150.' : _formatMuiErrorMessage(19));
}
export { default as createTheme, createMuiTheme } from "./createTheme.js";
export { default as unstable_createMuiStrictModeTheme } from "./createMuiStrictModeTheme.js";
export { default as createStyles } from "./createStyles.js";
export { getUnit as unstable_getUnit, toUnitless as unstable_toUnitless } from "./cssUtils.js";
export { default as responsiveFontSizes } from "./responsiveFontSizes.js";
export { default as createTransitions, duration, easing } from "./createTransitions.js";
export { default as createColorScheme } from "./createColorScheme.js";
export { default as useTheme } from "./useTheme.js";
export { default as useThemeProps } from "./useThemeProps.js";
export { default as styled } from "./styled.js";
export { default as experimentalStyled } from "./styled.js";
export { default as ThemeProvider } from "./ThemeProvider.js";
export { StyledEngineProvider } from '@mui/system';
// The legacy utilities from @mui/styles
// These are just empty functions that throws when invoked
export { default as makeStyles } from "./makeStyles.js";
export { default as withStyles } from "./withStyles.js";
export { default as withTheme } from "./withTheme.js";
export * from "./ThemeProviderWithVars.js";
export { default as extendTheme } from "./createThemeWithVars.js";
export { default as experimental_extendTheme } from "./experimental_extendTheme.js"; // TODO: Remove in v7
export { default as getOverlayAlpha } from "./getOverlayAlpha.js";
export { default as shouldSkipGeneratingVar } from "./shouldSkipGeneratingVar.js";

// Private methods for creating parts of the theme
export { default as private_createTypography } from "./createTypography.js";
export { default as private_createMixins } from "./createMixins.js";
export { default as private_excludeVariablesFromRoot } from "./excludeVariablesFromRoot.js";