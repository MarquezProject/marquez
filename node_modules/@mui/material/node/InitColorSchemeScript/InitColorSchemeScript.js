"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.defaultConfig = exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _InitColorSchemeScript = _interopRequireDefault(require("@mui/system/InitColorSchemeScript"));
var _jsxRuntime = require("react/jsx-runtime");
const defaultConfig = exports.defaultConfig = {
  attribute: 'data-mui-color-scheme',
  colorSchemeStorageKey: 'mui-color-scheme',
  defaultLightColorScheme: 'light',
  defaultDarkColorScheme: 'dark',
  modeStorageKey: 'mui-mode'
};
var InitColorSchemeScript = exports.default = function InitColorSchemeScript(props) {
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_InitColorSchemeScript.default, {
    ...defaultConfig,
    ...props
  });
};