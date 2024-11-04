import { Breakpoints, Breakpoint } from '../createBreakpoints/createBreakpoints';
export declare const filterBreakpointKeys: (breakpointsKeys: Breakpoint[], responsiveKeys: string[]) => Breakpoint[];
interface Iterator<T> {
    (appendStyle: (responsiveStyles: Record<string, any>, style: object) => void, value: T): void;
}
export declare const traverseBreakpoints: <T = unknown>(breakpoints: Breakpoints, responsive: T | T[] | Record<string, any> | undefined, iterator: Iterator<T>) => void;
export {};
