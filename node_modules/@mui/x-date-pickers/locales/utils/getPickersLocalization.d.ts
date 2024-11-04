import { AdapterFormats, MuiPickersAdapter, PickerValidDate } from '../../models';
import { PickersLocaleText } from './pickersLocaleTextApi';
export declare const getPickersLocalization: (pickersTranslations: Partial<PickersLocaleText<any>>) => {
    components: {
        MuiLocalizationProvider: {
            defaultProps: {
                localeText: {
                    previousMonth?: string | undefined;
                    nextMonth?: string | undefined;
                    calendarWeekNumberHeaderLabel?: string | undefined;
                    calendarWeekNumberHeaderText?: string | undefined;
                    calendarWeekNumberAriaLabelText?: ((weekNumber: number) => string) | undefined;
                    calendarWeekNumberText?: ((weekNumber: number) => string) | undefined;
                    openPreviousView?: string | undefined;
                    openNextView?: string | undefined;
                    calendarViewSwitchingButtonAriaLabel?: ((currentView: import("@mui/x-date-pickers/models").DateView) => string) | undefined;
                    start?: string | undefined;
                    end?: string | undefined;
                    startDate?: string | undefined;
                    startTime?: string | undefined;
                    endDate?: string | undefined;
                    endTime?: string | undefined;
                    cancelButtonLabel?: string | undefined;
                    clearButtonLabel?: string | undefined;
                    okButtonLabel?: string | undefined;
                    todayButtonLabel?: string | undefined;
                    clockLabelText?: ((view: import("@mui/x-date-pickers/models").TimeView, time: any, utils: MuiPickersAdapter<any, any>, formattedTime?: string | null) => string) | undefined;
                    hoursClockNumberText?: ((hours: string) => string) | undefined;
                    minutesClockNumberText?: ((minutes: string) => string) | undefined;
                    secondsClockNumberText?: ((seconds: string) => string) | undefined;
                    selectViewText?: ((view: import("../../internals").TimeViewWithMeridiem) => string) | undefined;
                    openDatePickerDialogue?: ((date: any, utils: MuiPickersAdapter<any, any>, formattedDate: string | null) => string) | undefined;
                    openTimePickerDialogue?: ((date: any, utils: MuiPickersAdapter<any, any>, formattedTime: string | null) => string) | undefined;
                    fieldClearLabel?: string | undefined;
                    timeTableLabel?: string | undefined;
                    dateTableLabel?: string | undefined;
                    fieldYearPlaceholder?: ((params: {
                        digitAmount: number;
                        format: string;
                    }) => string) | undefined;
                    fieldMonthPlaceholder?: ((params: {
                        contentType: import("@mui/x-date-pickers/models").FieldSectionContentType;
                        format: string;
                    }) => string) | undefined;
                    fieldDayPlaceholder?: ((params: {
                        format: string;
                    }) => string) | undefined;
                    fieldWeekDayPlaceholder?: ((params: {
                        contentType: import("@mui/x-date-pickers/models").FieldSectionContentType;
                        format: string;
                    }) => string) | undefined;
                    fieldHoursPlaceholder?: ((params: {
                        format: string;
                    }) => string) | undefined;
                    fieldMinutesPlaceholder?: ((params: {
                        format: string;
                    }) => string) | undefined;
                    fieldSecondsPlaceholder?: ((params: {
                        format: string;
                    }) => string) | undefined;
                    fieldMeridiemPlaceholder?: ((params: {
                        format: string;
                    }) => string) | undefined;
                    year?: string | undefined;
                    month?: string | undefined;
                    day?: string | undefined;
                    weekDay?: string | undefined;
                    hours?: string | undefined;
                    minutes?: string | undefined;
                    seconds?: string | undefined;
                    meridiem?: string | undefined;
                    empty?: string | undefined;
                    datePickerToolbarTitle?: string | undefined;
                    timePickerToolbarTitle?: string | undefined;
                    dateTimePickerToolbarTitle?: string | undefined;
                    dateRangePickerToolbarTitle?: string | undefined;
                };
            };
        };
    };
};
export declare const buildGetOpenDialogAriaText: <TDate extends PickerValidDate>(params: {
    utils: MuiPickersAdapter<TDate>;
    formatKey: keyof AdapterFormats;
    contextTranslation: (date: TDate | null, utils: MuiPickersAdapter<TDate>, formattedValue: string | null) => string;
    propsTranslation: ((date: TDate | null, utils: MuiPickersAdapter<TDate>, formattedValue: string | null) => string) | undefined;
}) => (value: TDate | null) => string;
