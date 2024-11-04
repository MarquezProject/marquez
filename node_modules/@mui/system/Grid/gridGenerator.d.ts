import { Breakpoints } from '../createBreakpoints/createBreakpoints';
import { Spacing } from '../createTheme/createSpacing';
import { ResponsiveStyleValue } from '../styleFunctionSx';
import { GridDirection, GridOwnerState } from './GridProps';
interface Props {
    theme: {
        breakpoints: Breakpoints;
        spacing?: Spacing;
    };
    ownerState: GridOwnerState;
}
export declare const generateGridSizeStyles: ({ theme, ownerState }: Props) => {};
export declare const generateGridOffsetStyles: ({ theme, ownerState }: Props) => {};
export declare const generateGridColumnsStyles: ({ theme, ownerState }: Props) => {};
export declare const generateGridRowSpacingStyles: ({ theme, ownerState }: Props) => {};
export declare const generateGridColumnSpacingStyles: ({ theme, ownerState }: Props) => {};
export declare const generateGridDirectionStyles: ({ theme, ownerState }: Props) => {};
export declare const generateGridStyles: ({ ownerState }: Props) => {};
export declare const generateSizeClassNames: (size: GridOwnerState["size"]) => string[];
export declare const generateSpacingClassNames: (spacing: GridOwnerState["spacing"], smallestBreakpoint?: string) => string[];
export declare const generateDirectionClasses: (direction: ResponsiveStyleValue<GridDirection> | undefined) => string[];
export {};
