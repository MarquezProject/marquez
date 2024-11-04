'use client';

import systemUseThemeProps from '@mui/system/useThemeProps';
import defaultTheme from "./defaultTheme.js";
import THEME_ID from "./identifier.js";
export default function useThemeProps({
  props,
  name
}) {
  return systemUseThemeProps({
    props,
    name,
    defaultTheme,
    themeId: THEME_ID
  });
}