import * as React from 'react';
import { SupportedColorScheme } from './createThemeWithVars';
declare const useColorScheme: () => import("@mui/system").ColorSchemeContextValue<SupportedColorScheme>, deprecatedGetInitColorSchemeScript: typeof import("@mui/system/InitColorSchemeScript").default;
declare function Experimental_CssVarsProvider(props: any): React.JSX.Element;
declare const getInitColorSchemeScript: typeof deprecatedGetInitColorSchemeScript;
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
export declare const CssVarsProvider: (props: React.PropsWithChildren<Partial<import("@mui/system").CssVarsProviderConfig<SupportedColorScheme>> & {
    theme?: {
        cssVariables?: false;
        cssVarPrefix?: string;
        colorSchemes: Partial<Record<SupportedColorScheme, any>>;
        colorSchemeSelector?: "media" | "class" | "data" | string;
    } | {
        $$material: {
            cssVariables?: false;
            cssVarPrefix?: string;
            colorSchemes: Partial<Record<SupportedColorScheme, any>>;
            colorSchemeSelector?: "media" | "class" | "data" | string;
        };
    } | undefined;
    defaultMode?: "light" | "dark" | "system";
    documentNode?: Document | null;
    colorSchemeNode?: Element | null;
    storageWindow?: Window | null;
    disableNestedContext?: boolean;
    disableStyleSheetGeneration?: boolean;
}>) => React.JSX.Element;
export { useColorScheme, getInitColorSchemeScript, Experimental_CssVarsProvider };
