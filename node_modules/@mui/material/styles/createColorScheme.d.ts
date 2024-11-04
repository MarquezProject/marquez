import type { ColorSystemOptions } from './createThemeWithVars';
export declare function getOpacity(mode: 'light' | 'dark'): {
    inputPlaceholder: number;
    inputUnderline: number;
    switchTrackDisabled: number;
    switchTrack: number;
};
export declare function getOverlays(mode: 'light' | 'dark'): string[];
export default function createColorScheme(options: ColorSystemOptions): ColorSystemOptions;
