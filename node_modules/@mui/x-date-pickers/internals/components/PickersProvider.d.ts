import * as React from 'react';
import { PickerValidDate } from '../../models';
import { PickersInputLocaleText } from '../../locales';
export declare const PickersContext: React.Context<PickersContextValue | null>;
/**
 * Provides the context for the various parts of a picker component:
 * - contextValue: the context for the picker sub-components.
 * - localizationProvider: the translations passed through the props and through a parent LocalizationProvider.
 *
 * @ignore - do not document.
 */
export declare function PickersProvider<TDate extends PickerValidDate>(props: PickersFieldProviderProps<TDate>): React.JSX.Element;
interface PickersFieldProviderProps<TDate extends PickerValidDate> {
    contextValue: PickersContextValue;
    localeText: PickersInputLocaleText<TDate> | undefined;
    children: React.ReactNode;
}
export interface PickersContextValue {
    /**
     * Open the picker.
     * @param {React.UIEvent} event The DOM event that triggered the change.
     */
    onOpen: (event: React.UIEvent) => void;
    /**
     * Close the picker.
     * @param {React.UIEvent} event The DOM event that triggered the change.
     */
    onClose: (event: React.UIEvent) => void;
    /**
     * `true` if the picker is open, `false` otherwise.
     */
    open: boolean;
}
export {};
