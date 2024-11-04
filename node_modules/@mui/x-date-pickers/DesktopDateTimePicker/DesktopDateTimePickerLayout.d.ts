import * as React from 'react';
import { PickersLayoutProps } from '../PickersLayout';
import { PickerValidDate } from '../models';
import { DateOrTimeViewWithMeridiem } from '../internals/models/common';
type DesktopDateTimePickerLayoutComponent = (<TValue, TDate extends PickerValidDate, TView extends DateOrTimeViewWithMeridiem>(props: PickersLayoutProps<TValue, TDate, TView> & React.RefAttributes<HTMLDivElement>) => React.JSX.Element) & {
    propTypes?: any;
};
/**
 * @ignore - internal component.
 */
declare const DesktopDateTimePickerLayout: DesktopDateTimePickerLayoutComponent;
export { DesktopDateTimePickerLayout };
