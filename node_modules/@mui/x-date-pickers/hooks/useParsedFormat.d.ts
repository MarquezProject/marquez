import { PickerValidDate } from '../models';
import type { UseFieldInternalProps } from '../internals/hooks/useField';
interface UseParsedFormatParameters extends Pick<UseFieldInternalProps<any, any, any, any, any>, 'format' | 'formatDensity' | 'shouldRespectLeadingZeros'> {
}
/**
 * Returns the parsed format to be rendered in the field when there is no value or in other parts of the Picker.
 * This format is localized (e.g: `AAAA` for the year with the French locale) and cannot be parsed by your date library.
 * @param {object} The parameters needed to build the placeholder.
 * @param {string} params.format Format of the date to use.
 * @param {'dense' | 'spacious'} params.formatDensity Density of the format (setting `formatDensity` to `"spacious"` will add a space before and after each `/`, `-` and `.` character).
 * @param {boolean} params.shouldRespectLeadingZeros If `true`, the format will respect the leading zeroes, if `false`, the format will always add leading zeroes.
 * @returns
 */
export declare const useParsedFormat: <TDate extends PickerValidDate>(parameters: UseParsedFormatParameters) => string;
export {};
