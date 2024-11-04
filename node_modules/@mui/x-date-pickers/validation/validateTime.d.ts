import { Validator } from './useValidation';
import { BaseTimeValidationProps, TimeValidationProps } from '../internals/models/validation';
import { PickerValidDate, TimeValidationError } from '../models';
export interface ValidateTimeProps<TDate extends PickerValidDate> extends Required<BaseTimeValidationProps>, TimeValidationProps<TDate> {
}
export declare const validateTime: Validator<any | null, any, TimeValidationError, ValidateTimeProps<any>>;
