import { PaletteColorOptions, SimplePaletteColorOptions } from '../styles/createPalette';
type AdditionalPropertiesToCheck = (keyof Omit<SimplePaletteColorOptions, 'main'>)[];
/**
 * Creates a filter function used to filter simple palette color options.
 * The minimum requirement is that the object has a "main" property of type string, this is always checked.
 * Optionally, you can pass additional properties to check.
 *
 * @param additionalPropertiesToCheck - Array containing "light", "dark", and/or "contrastText"
 * @returns ([, value]: [any, PaletteColorOptions]) => boolean
 */
export default function createSimplePaletteValueFilter(additionalPropertiesToCheck?: AdditionalPropertiesToCheck): ([, value]: [any, PaletteColorOptions]) => boolean;
export {};
