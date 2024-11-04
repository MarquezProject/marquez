"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useIsLandscape = void 0;
var React = _interopRequireWildcard(require("react"));
var _utils = require("@mui/utils");
var _utils2 = require("../utils/utils");
function getOrientation() {
  if (typeof window === 'undefined') {
    return 'portrait';
  }
  if (window.screen && window.screen.orientation && window.screen.orientation.angle) {
    return Math.abs(window.screen.orientation.angle) === 90 ? 'landscape' : 'portrait';
  }

  // Support IOS safari
  if (window.orientation) {
    return Math.abs(Number(window.orientation)) === 90 ? 'landscape' : 'portrait';
  }
  return 'portrait';
}
const useIsLandscape = (views, customOrientation) => {
  const [orientation, setOrientation] = React.useState(getOrientation);
  (0, _utils.unstable_useEnhancedEffect)(() => {
    const eventHandler = () => {
      setOrientation(getOrientation());
    };
    window.addEventListener('orientationchange', eventHandler);
    return () => {
      window.removeEventListener('orientationchange', eventHandler);
    };
  }, []);
  if ((0, _utils2.arrayIncludes)(views, ['hours', 'minutes', 'seconds'])) {
    // could not display 13:34:44 in landscape mode
    return false;
  }
  const orientationToUse = customOrientation || orientation;
  return orientationToUse === 'landscape';
};
exports.useIsLandscape = useIsLandscape;