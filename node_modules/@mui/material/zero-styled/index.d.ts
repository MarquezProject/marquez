import * as React from 'react';
import { Interpolation } from '@mui/system';
import { extendSxProp } from '@mui/system/styleFunctionSx';
import { Theme } from '../styles/createTheme';
import useTheme from '../styles/useTheme';
export { css, keyframes } from '@mui/system';
export { default as styled } from '../styles/styled';
export declare function globalCss(styles: Interpolation<{
    theme: Theme;
}>): (props: Record<string, any>) => React.JSX.Element;
export declare function internal_createExtendSxProp(): typeof extendSxProp;
export { useTheme };
