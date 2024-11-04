import { DefaultCssVarsTheme } from './prepareCssVars';
interface Theme extends DefaultCssVarsTheme {
    cssVarPrefix?: string;
    colorSchemeSelector?: 'media' | string;
    shouldSkipGeneratingVar?: (objectPathKeys: Array<string>, value: string | number) => boolean;
}
declare function createCssVarsTheme<T extends Theme, ThemeVars extends Record<string, any>>({ colorSchemeSelector, ...theme }: T): T & {
    vars: ThemeVars;
    generateThemeVars: () => ThemeVars;
    generateStyleSheets: () => Record<string, any>[];
};
export default createCssVarsTheme;
