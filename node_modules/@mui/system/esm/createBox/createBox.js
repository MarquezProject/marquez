'use client';

import * as React from 'react';
import clsx from 'clsx';
import styled from '@mui/styled-engine';
import styleFunctionSx, { extendSxProp } from "../styleFunctionSx/index.js";
import useTheme from "../useTheme/index.js";
import { jsx as _jsx } from "react/jsx-runtime";
export default function createBox(options = {}) {
  const {
    themeId,
    defaultTheme,
    defaultClassName = 'MuiBox-root',
    generateClassName
  } = options;
  const BoxRoot = styled('div', {
    shouldForwardProp: prop => prop !== 'theme' && prop !== 'sx' && prop !== 'as'
  })(styleFunctionSx);
  const Box = /*#__PURE__*/React.forwardRef(function Box(inProps, ref) {
    const theme = useTheme(defaultTheme);
    const {
      className,
      component = 'div',
      ...other
    } = extendSxProp(inProps);
    return /*#__PURE__*/_jsx(BoxRoot, {
      as: component,
      ref: ref,
      className: clsx(className, generateClassName ? generateClassName(defaultClassName) : defaultClassName),
      theme: themeId ? theme[themeId] || theme : theme,
      ...other
    });
  });
  return Box;
}