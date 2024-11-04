import * as React from 'react';
import { TouchRippleActions } from '../ButtonBase/TouchRipple';
type ControlledPromise<T = unknown> = Promise<T> & {
    resolve: Function;
    reject: Function;
};
/**
 * Lazy initialization container for the Ripple instance. This improves
 * performance by delaying mounting the ripple until it's needed.
 */
export declare class LazyRipple {
    /** React ref to the ripple instance */
    ref: React.MutableRefObject<TouchRippleActions | null>;
    /** If the ripple component should be mounted */
    shouldMount: boolean;
    /** Promise that resolves when the ripple component is mounted */
    private mounted;
    /** If the ripple component has been mounted */
    private didMount;
    /** React state hook setter */
    private setShouldMount;
    static create(): LazyRipple;
    static use(): LazyRipple;
    constructor();
    mount(): ControlledPromise<unknown>;
    mountEffect: () => void;
    start(...args: Parameters<TouchRippleActions['start']>): void;
    stop(...args: Parameters<TouchRippleActions['stop']>): void;
    pulsate(...args: Parameters<TouchRippleActions['pulsate']>): void;
}
export default function useLazyRipple(): LazyRipple;
export {};
