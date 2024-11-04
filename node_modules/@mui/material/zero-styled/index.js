import * as React from 'react';
import { extendSxProp } from '@mui/system/styleFunctionSx';
import useTheme from "../styles/useTheme.js";
import GlobalStyles from "../GlobalStyles/index.js";
import { jsx as _jsx } from "react/jsx-runtime";
export { css, keyframes } from '@mui/system';
export { default as styled } from "../styles/styled.js";
export function globalCss(styles) {
  return function GlobalStylesWrapper(props) {
    return (
      /*#__PURE__*/
      // Pigment CSS `globalCss` support callback with theme inside an object but `GlobalStyles` support theme as a callback value.
      _jsx(GlobalStyles, {
        styles: typeof styles === 'function' ? theme => styles({
          theme,
          ...props
        }) : styles
      })
    );
  };
}

// eslint-disable-next-line @typescript-eslint/naming-convention
export function internal_createExtendSxProp() {
  return extendSxProp;
}
export { useTheme };