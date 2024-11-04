import { ValidateDateProps } from '../validation';
import { PickerValidDate, TimezoneProps } from '../models';
import { DefaultizedProps } from '../internals/models/helpers';
export declare const useIsDateDisabled: <TDate extends PickerValidDate>({ shouldDisableDate, shouldDisableMonth, shouldDisableYear, minDate, maxDate, disableFuture, disablePast, timezone, }: ValidateDateProps<TDate> & DefaultizedProps<TimezoneProps, "timezone">) => (day: TDate | null) => boolean;
