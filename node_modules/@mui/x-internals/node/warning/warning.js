"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.clearWarningsCache = clearWarningsCache;
exports.warnOnce = warnOnce;
const warnedOnceCache = new Set();

// TODO move to @base_ui/internals. Base UI, etc. need this helper.
function warnOnce(message, gravity = 'warning') {
  if (process.env.NODE_ENV === 'production') {
    return;
  }
  const cleanMessage = Array.isArray(message) ? message.join('\n') : message;
  if (!warnedOnceCache.has(cleanMessage)) {
    warnedOnceCache.add(cleanMessage);
    if (gravity === 'error') {
      console.error(cleanMessage);
    } else {
      console.warn(cleanMessage);
    }
  }
}
function clearWarningsCache() {
  warnedOnceCache.clear();
}