import * as React from 'react';
import { DefaultTheme } from '@mui/system';
export interface ThemeProviderNoVarsProps<Theme = DefaultTheme> {
    children?: React.ReactNode;
    theme: Partial<Theme> | ((outerTheme: Theme) => Theme);
}
export default function ThemeProviderNoVars<Theme = DefaultTheme>({ theme: themeInput, ...props }: ThemeProviderNoVarsProps<Theme>): React.ReactElement<ThemeProviderNoVarsProps<Theme>>;
