'use client';

import * as React from 'react';
import ThemeProviderNoVars from "./ThemeProviderNoVars.js";
import { CssVarsProvider } from "./ThemeProviderWithVars.js";
import THEME_ID from "./identifier.js";
import { jsx as _jsx } from "react/jsx-runtime";
export default function ThemeProvider({
  theme,
  ...props
}) {
  if (typeof theme === 'function') {
    return /*#__PURE__*/_jsx(ThemeProviderNoVars, {
      theme: theme,
      ...props
    });
  }
  const muiTheme = THEME_ID in theme ? theme[THEME_ID] : theme;
  if (!('colorSchemes' in muiTheme)) {
    return /*#__PURE__*/_jsx(ThemeProviderNoVars, {
      theme: theme,
      ...props
    });
  }
  return /*#__PURE__*/_jsx(CssVarsProvider, {
    theme: theme,
    ...props
  });
}