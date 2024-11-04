import { Breakpoints } from '../createBreakpoints/createBreakpoints';
interface ContainerQueries {
    up: Breakpoints['up'];
    down: Breakpoints['down'];
    between: Breakpoints['between'];
    only: Breakpoints['only'];
    not: Breakpoints['not'];
}
export interface CssContainerQueries {
    containerQueries: ((name: string) => ContainerQueries) & ContainerQueries;
}
/**
 * For using in `sx` prop to sort the breakpoint from low to high.
 * Note: this function does not work and will not support multiple units.
 *       e.g. input: { '@container (min-width:300px)': '1rem', '@container (min-width:40rem)': '2rem' }
 *            output: { '@container (min-width:40rem)': '2rem', '@container (min-width:300px)': '1rem' } // since 40 < 300 eventhough 40rem > 300px
 */
export declare function sortContainerQueries(theme: Partial<CssContainerQueries>, css: Record<string, any>): Record<string, any>;
export declare function isCqShorthand(breakpointKeys: string[], value: string): boolean;
export declare function getContainerQuery(theme: CssContainerQueries, shorthand: string): string | null;
export default function cssContainerQueries<T extends {
    breakpoints: Breakpoints;
}>(themeInput: T): T & CssContainerQueries;
export {};
