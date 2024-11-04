'use client';

import useLazyRef from "../useLazyRef/useLazyRef.js";
import useOnMount from "../useOnMount/useOnMount.js";
export class Timeout {
  static create() {
    return new Timeout();
  }
  currentId = null;

  /**
   * Executes `fn` after `delay`, clearing any previously scheduled call.
   */
  start(delay, fn) {
    this.clear();
    this.currentId = setTimeout(() => {
      this.currentId = null;
      fn();
    }, delay);
  }
  clear = () => {
    if (this.currentId !== null) {
      clearTimeout(this.currentId);
      this.currentId = null;
    }
  };
  disposeEffect = () => {
    return this.clear;
  };
}
export default function useTimeout() {
  const timeout = useLazyRef(Timeout.create).current;
  useOnMount(timeout.disposeEffect);
  return timeout;
}