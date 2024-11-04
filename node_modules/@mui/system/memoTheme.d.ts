import { CSSInterpolation } from '@mui/styled-engine';
type ThemeStyleFunction<T> = (props: {
    theme: T;
}) => CSSInterpolation;
/**
 * Memoize style function on theme.
 * Intended to be used in styled() calls that only need access to the theme.
 */
export default function unstable_memoTheme<T>(styleFn: ThemeStyleFunction<T>): (props: {
    theme: T;
}) => string | number | boolean | import("@mui/styled-engine").ComponentSelector | import("@mui/styled-engine").Keyframes | import("@mui/styled-engine").SerializedStyles | import("@mui/styled-engine").CSSObject | import("@mui/styled-engine").ArrayCSSInterpolation | null;
export {};
