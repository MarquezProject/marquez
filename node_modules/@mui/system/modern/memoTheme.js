import preprocessStyles from "./preprocessStyles.js";

/* eslint-disable @typescript-eslint/naming-convention */

// We need to pass an argument as `{ theme }` for PigmentCSS, but we don't want to
// allocate more objects.
const arg = {
  theme: undefined
};

/**
 * Memoize style function on theme.
 * Intended to be used in styled() calls that only need access to the theme.
 */
export default function unstable_memoTheme(styleFn) {
  let lastValue;
  let lastTheme;
  return function styleMemoized(props) {
    let value = lastValue;
    if (value === undefined || props.theme !== lastTheme) {
      arg.theme = props.theme;
      value = preprocessStyles(styleFn(arg));
      lastValue = value;
      lastTheme = props.theme;
    }
    return value;
  };
}