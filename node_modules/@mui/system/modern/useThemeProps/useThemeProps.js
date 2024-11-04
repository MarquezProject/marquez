'use client';

import getThemeProps from "./getThemeProps.js";
import useTheme from "../useTheme/index.js";
export default function useThemeProps({
  props,
  name,
  defaultTheme,
  themeId
}) {
  let theme = useTheme(defaultTheme);
  if (themeId) {
    theme = theme[themeId] || theme;
  }
  return getThemeProps({
    theme,
    name,
    props
  });
}