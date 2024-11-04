import { FieldSection, PickerOwnerState } from '../../../models';
import type { UsePickerProps } from './usePicker.types';
import { UsePickerValueResponse } from './usePickerValue.types';
interface UsePickerOwnerStateParameters<TValue> {
    props: UsePickerProps<TValue, any, any, any, any, any>;
    pickerValueResponse: UsePickerValueResponse<TValue, FieldSection, any>;
}
export declare function usePickerOwnerState<TValue>(parameters: UsePickerOwnerStateParameters<TValue>): PickerOwnerState<TValue>;
export {};
