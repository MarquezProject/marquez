'use client';

import createTheme from "../createTheme/index.js";
import useThemeWithoutDefault from "../useThemeWithoutDefault/index.js";
export const systemDefaultTheme = createTheme();
function useTheme(defaultTheme = systemDefaultTheme) {
  return useThemeWithoutDefault(defaultTheme);
}
export default useTheme;