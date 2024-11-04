import * as React from 'react';
import { FormControlState } from '@mui/material/FormControl';
import { PickersInputBaseProps } from './PickersInputBase.types';
export declare const PickersInputBaseRoot: import("@emotion/styled").StyledComponent<import("@mui/system").MUIStyledCommonProps<import("@mui/material").Theme> & {
    ownerState: OwnerStateType;
}, Pick<React.DetailedHTMLProps<React.HTMLAttributes<HTMLDivElement>, HTMLDivElement>, keyof React.ClassAttributes<HTMLDivElement> | keyof React.HTMLAttributes<HTMLDivElement>>, {}>;
export declare const PickersInputBaseSectionsContainer: import("@emotion/styled").StyledComponent<Pick<import("@mui/system").MUIStyledCommonProps<import("@mui/material").Theme> & Pick<React.DetailedHTMLProps<React.HTMLAttributes<HTMLDivElement>, HTMLDivElement>, keyof React.ClassAttributes<HTMLDivElement> | keyof React.HTMLAttributes<HTMLDivElement>>, keyof React.ClassAttributes<HTMLDivElement> | keyof React.HTMLAttributes<HTMLDivElement> | keyof import("@mui/system").MUIStyledCommonProps<import("@mui/material").Theme>> & import("@mui/system").MUIStyledCommonProps<import("@mui/material").Theme> & {
    ownerState: OwnerStateType;
}, {}, {}>;
interface OwnerStateType extends FormControlState, Omit<PickersInputBaseProps, keyof FormControlState> {
    isRtl: boolean;
}
/**
 * @ignore - internal component.
 */
declare const PickersInputBase: React.ForwardRefExoticComponent<Omit<PickersInputBaseProps, "ref"> & React.RefAttributes<HTMLDivElement>>;
export { PickersInputBase };
