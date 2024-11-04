declare const _default: <T extends {
    rootSelector?: string;
    colorSchemeSelector?: "media" | "class" | "data" | string;
    colorSchemes?: Record<string, any>;
    defaultColorScheme?: string;
    cssVarPrefix?: string;
}>(theme: T) => (colorScheme: keyof T["colorSchemes"] | undefined, css: Record<string, any>) => string | {
    [x: string]: Record<string, any>;
    "@media (prefers-color-scheme: dark)": {
        [x: string]: Record<string, any>;
    };
} | {
    [x: string]: Record<string, any>;
    "@media (prefers-color-scheme: dark)"?: undefined;
};
export default _default;
