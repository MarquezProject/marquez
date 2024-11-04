'use client';

import createStyled from '@mui/system/createStyled';
import defaultTheme from "./defaultTheme.js";
import THEME_ID from "./identifier.js";
import rootShouldForwardProp from "./rootShouldForwardProp.js";
export { default as slotShouldForwardProp } from "./slotShouldForwardProp.js";
export { default as rootShouldForwardProp } from "./rootShouldForwardProp.js";
const styled = createStyled({
  themeId: THEME_ID,
  defaultTheme,
  rootShouldForwardProp
});
export default styled;