import { UseFieldParams, UseFieldResponse, UseFieldCommonForwardedProps, UseFieldInternalProps, UseFieldForwardedProps } from './useField.types';
import { PickerValidDate, FieldSection } from '../../../models';
export declare const useField: <TValue, TDate extends PickerValidDate, TSection extends FieldSection, TEnableAccessibleFieldDOMStructure extends boolean, TForwardedProps extends UseFieldCommonForwardedProps & UseFieldForwardedProps<TEnableAccessibleFieldDOMStructure>, TInternalProps extends UseFieldInternalProps<any, any, any, TEnableAccessibleFieldDOMStructure, any> & {
    minutesStep?: number;
}>(params: UseFieldParams<TValue, TDate, TSection, TEnableAccessibleFieldDOMStructure, TForwardedProps, TInternalProps>) => UseFieldResponse<TEnableAccessibleFieldDOMStructure, TForwardedProps>;
