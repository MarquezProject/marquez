"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
/**
 * If `componentProps` is a function, calls it with the provided `ownerState`.
 * Otherwise, just returns `componentProps`.
 */
function resolveComponentProps(componentProps, ownerState, slotState) {
  if (typeof componentProps === 'function') {
    return componentProps(ownerState, slotState);
  }
  return componentProps;
}
var _default = exports.default = resolveComponentProps;