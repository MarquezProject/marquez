"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Timeout = void 0;
exports.default = useTimeout;
var _useLazyRef = _interopRequireDefault(require("../useLazyRef/useLazyRef"));
var _useOnMount = _interopRequireDefault(require("../useOnMount/useOnMount"));
class Timeout {
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
exports.Timeout = Timeout;
function useTimeout() {
  const timeout = (0, _useLazyRef.default)(Timeout.create).current;
  (0, _useOnMount.default)(timeout.disposeEffect);
  return timeout;
}