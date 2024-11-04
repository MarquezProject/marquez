import { Validator } from './useValidation';
import { BaseDateValidationProps, DayValidationProps, MonthValidationProps, YearValidationProps } from '../internals/models/validation';
import { DateValidationError, PickerValidDate } from '../models';
export interface ValidateDateProps<TDate extends PickerValidDate> extends DayValidationProps<TDate>, MonthValidationProps<TDate>, YearValidationProps<TDate>, Required<BaseDateValidationProps<TDate>> {
}
export declare const validateDate: Validator<any | null, any, DateValidationError, ValidateDateProps<any>>;
