export interface DefaultCssVarsTheme {
    colorSchemes?: Record<string, any>;
    defaultColorScheme?: string;
}
declare function prepareCssVars<T extends DefaultCssVarsTheme, ThemeVars extends Record<string, any>>(theme: T, parserConfig?: {
    prefix?: string;
    colorSchemeSelector?: 'media' | 'class' | 'data' | string;
    disableCssColorScheme?: boolean;
    shouldSkipGeneratingVar?: (objectPathKeys: Array<string>, value: string | number) => boolean;
    getSelector?: (colorScheme: keyof T['colorSchemes'] | undefined, css: Record<string, any>) => string | Record<string, any>;
}): {
    vars: ThemeVars;
    generateThemeVars: () => ThemeVars;
    generateStyleSheets: () => Record<string, any>[];
};
export default prepareCssVars;
