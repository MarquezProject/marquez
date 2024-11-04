"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
Object.defineProperty(exports, "css", {
  enumerable: true,
  get: function () {
    return _system.css;
  }
});
exports.globalCss = globalCss;
exports.internal_createExtendSxProp = internal_createExtendSxProp;
Object.defineProperty(exports, "keyframes", {
  enumerable: true,
  get: function () {
    return _system.keyframes;
  }
});
Object.defineProperty(exports, "styled", {
  enumerable: true,
  get: function () {
    return _styled.default;
  }
});
Object.defineProperty(exports, "useTheme", {
  enumerable: true,
  get: function () {
    return _useTheme.default;
  }
});
var React = _interopRequireWildcard(require("react"));
var _styleFunctionSx = require("@mui/system/styleFunctionSx");
var _useTheme = _interopRequireDefault(require("../styles/useTheme"));
var _GlobalStyles = _interopRequireDefault(require("../GlobalStyles"));
var _jsxRuntime = require("react/jsx-runtime");
var _system = require("@mui/system");
var _styled = _interopRequireDefault(require("../styles/styled"));
function globalCss(styles) {
  return function GlobalStylesWrapper(props) {
    return (
      /*#__PURE__*/
      // Pigment CSS `globalCss` support callback with theme inside an object but `GlobalStyles` support theme as a callback value.
      (0, _jsxRuntime.jsx)(_GlobalStyles.default, {
        styles: typeof styles === 'function' ? theme => styles({
          theme,
          ...props
        }) : styles
      })
    );
  };
}

// eslint-disable-next-line @typescript-eslint/naming-convention
function internal_createExtendSxProp() {
  return _styleFunctionSx.extendSxProp;
}