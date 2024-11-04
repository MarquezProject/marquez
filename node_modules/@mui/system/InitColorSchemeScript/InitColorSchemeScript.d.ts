/**
 * Split this component for RSC import
 */
import * as React from 'react';
export declare const DEFAULT_MODE_STORAGE_KEY = "mode";
export declare const DEFAULT_COLOR_SCHEME_STORAGE_KEY = "color-scheme";
export declare const DEFAULT_ATTRIBUTE = "data-color-scheme";
export interface InitColorSchemeScriptProps {
    /**
     * The default mode when the storage is empty (user's first visit).
     * @default 'system'
     */
    defaultMode?: 'system' | 'light' | 'dark';
    /**
     * The default color scheme to be used on the light mode.
     * @default 'light'
     */
    defaultLightColorScheme?: string;
    /**
     * The default color scheme to be used on the dark mode.
     * * @default 'dark'
     */
    defaultDarkColorScheme?: string;
    /**
     * The node (provided as string) used to attach the color-scheme attribute.
     * @default 'document.documentElement'
     */
    colorSchemeNode?: string;
    /**
     * localStorage key used to store `mode`.
     * @default 'mode'
     */
    modeStorageKey?: string;
    /**
     * localStorage key used to store `colorScheme`.
     * @default 'color-scheme'
     */
    colorSchemeStorageKey?: string;
    /**
     * DOM attribute for applying color scheme.
     * @default 'data-color-scheme'
     * @example '.mode-%s' // for class based color scheme
     * @example '[data-mode-%s]' // for data-attribute without '='
     */
    attribute?: 'class' | 'data' | string;
    /**
     * Nonce string to pass to the inline script for CSP headers.
     */
    nonce?: string | undefined;
}
export default function InitColorSchemeScript(options?: InitColorSchemeScriptProps): React.JSX.Element;
