'use client';

import * as React from 'react';
import { ThemeProvider as SystemThemeProvider } from '@mui/system';
import THEME_ID from "./identifier.js";
import { jsx as _jsx } from "react/jsx-runtime";
export default function ThemeProviderNoVars({
  theme: themeInput,
  ...props
}) {
  const scopedTheme = THEME_ID in themeInput ? themeInput[THEME_ID] : undefined;
  return /*#__PURE__*/_jsx(SystemThemeProvider, {
    ...props,
    themeId: scopedTheme ? THEME_ID : undefined,
    theme: scopedTheme || themeInput
  });
}