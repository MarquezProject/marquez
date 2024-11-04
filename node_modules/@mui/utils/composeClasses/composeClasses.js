"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = composeClasses;
/* eslint no-restricted-syntax: 0, prefer-template: 0, guard-for-in: 0
   ---
   These rules are preventing the performance optimizations below.
 */

function composeClasses(slots, getUtilityClass, classes = undefined) {
  const output = {};
  for (const slotName in slots) {
    const slot = slots[slotName];
    let buffer = '';
    let start = true;
    for (let i = 0; i < slot.length; i += 1) {
      const value = slot[i];
      if (value) {
        buffer += (start === true ? '' : ' ') + getUtilityClass(value);
        start = false;
        if (classes && classes[value]) {
          buffer += ' ' + classes[value];
        }
      }
    }
    output[slotName] = buffer;
  }
  return output;
}