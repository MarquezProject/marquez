'use client';

import { unstable_ClassNameGenerator as ClassNameGenerator } from '@mui/utils';
export { default as capitalize } from "./capitalize.js";
export { default as createChainedFunction } from "./createChainedFunction.js";
export { default as createSvgIcon } from "./createSvgIcon.js";
export { default as debounce } from "./debounce.js";
export { default as deprecatedPropType } from "./deprecatedPropType.js";
export { default as isMuiElement } from "./isMuiElement.js";
export { default as unstable_memoTheme } from "./memoTheme.js";
export { default as ownerDocument } from "./ownerDocument.js";
export { default as ownerWindow } from "./ownerWindow.js";
export { default as requirePropFactory } from "./requirePropFactory.js";
export { default as setRef } from "./setRef.js";
export { default as unstable_useEnhancedEffect } from "./useEnhancedEffect.js";
export { default as unstable_useId } from "./useId.js";
export { default as unsupportedProp } from "./unsupportedProp.js";
export { default as useControlled } from "./useControlled.js";
export { default as useEventCallback } from "./useEventCallback.js";
export { default as useForkRef } from "./useForkRef.js";
// TODO: remove this export once ClassNameGenerator is stable
// eslint-disable-next-line @typescript-eslint/naming-convention
export const unstable_ClassNameGenerator = {
  configure: generator => {
    if (process.env.NODE_ENV !== 'production') {
      console.warn(['MUI: `ClassNameGenerator` import from `@mui/material/utils` is outdated and might cause unexpected issues.', '', "You should use `import { unstable_ClassNameGenerator } from '@mui/material/className'` instead", '', 'The detail of the issue: https://github.com/mui/material-ui/issues/30011#issuecomment-1024993401', '', 'The updated documentation: https://mui.com/guides/classname-generator/'].join('\n'));
    }
    ClassNameGenerator.configure(generator);
  }
};