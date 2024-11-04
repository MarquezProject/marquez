import * as React from 'react';
import { PickersLayoutProps } from './PickersLayout.types';
import { DateOrTimeViewWithMeridiem } from '../internals/models';
import { PickerValidDate } from '../models';
export declare const PickersLayoutRoot: import("@emotion/styled").StyledComponent<import("@mui/system").MUIStyledCommonProps<import("@mui/material").Theme> & {
    ownerState: PickersLayoutProps<any, any, any>;
}, Pick<React.DetailedHTMLProps<React.HTMLAttributes<HTMLDivElement>, HTMLDivElement>, keyof React.ClassAttributes<HTMLDivElement> | keyof React.HTMLAttributes<HTMLDivElement>>, {}>;
export declare const PickersLayoutContentWrapper: import("@emotion/styled").StyledComponent<import("@mui/system").MUIStyledCommonProps<import("@mui/material").Theme>, Pick<React.DetailedHTMLProps<React.HTMLAttributes<HTMLDivElement>, HTMLDivElement>, keyof React.ClassAttributes<HTMLDivElement> | keyof React.HTMLAttributes<HTMLDivElement>>, {}>;
type PickersLayoutComponent = (<TValue, TDate extends PickerValidDate, TView extends DateOrTimeViewWithMeridiem>(props: PickersLayoutProps<TValue, TDate, TView> & React.RefAttributes<HTMLDivElement>) => React.JSX.Element) & {
    propTypes?: any;
};
/**
 * Demos:
 *
 * - [Custom layout](https://mui.com/x/react-date-pickers/custom-layout/)
 *
 * API:
 *
 * - [PickersLayout API](https://mui.com/x/api/date-pickers/pickers-layout/)
 */
declare const PickersLayout: PickersLayoutComponent;
export { PickersLayout };
