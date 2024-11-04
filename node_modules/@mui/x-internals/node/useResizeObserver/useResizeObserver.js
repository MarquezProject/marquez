"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useResizeObserver = useResizeObserver;
var React = _interopRequireWildcard(require("react"));
var _utils = require("@mui/utils");
const isDevEnvironment = process.env.NODE_ENV === 'development';
const noop = () => {};
function useResizeObserver(ref, fn, enabled) {
  const fnRef = React.useRef(null);
  fnRef.current = fn;
  (0, _utils.unstable_useEnhancedEffect)(() => {
    if (enabled === false || typeof ResizeObserver === 'undefined') {
      return noop;
    }
    let frameID = 0;
    const target = ref.current;
    const observer = new ResizeObserver(entries => {
      // See https://github.com/mui/mui-x/issues/8733
      // In dev, we avoid the React warning by moving the task to the next frame.
      // In prod, we want the task to run in the same frame as to avoid tear.
      if (isDevEnvironment) {
        frameID = requestAnimationFrame(() => {
          fnRef.current(entries);
        });
      } else {
        fnRef.current(entries);
      }
    });
    if (target) {
      observer.observe(target);
    }
    return () => {
      if (frameID) {
        cancelAnimationFrame(frameID);
      }
      observer.disconnect();
    };
  }, [ref, enabled]);
}