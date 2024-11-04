import { Validator } from './useValidation';
import { ValidateDateProps } from './validateDate';
import { ValidateTimeProps } from './validateTime';
import { DateTimeValidationError, PickerValidDate } from '../models';
export interface ValidateDateTimeProps<TDate extends PickerValidDate> extends ValidateDateProps<TDate>, ValidateTimeProps<TDate> {
}
export declare const validateDateTime: Validator<any | null, any, DateTimeValidationError, ValidateDateTimeProps<any>>;
