"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _system = require("@mui/system");
var _defaultTheme = _interopRequireDefault(require("../styles/defaultTheme"));
var _identifier = _interopRequireDefault(require("../styles/identifier"));
var _jsxRuntime = require("react/jsx-runtime");
function GlobalStyles(props) {
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_system.GlobalStyles, {
    ...props,
    defaultTheme: _defaultTheme.default,
    themeId: _identifier.default
  });
}
process.env.NODE_ENV !== "production" ? GlobalStyles.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The styles you want to apply globally.
   */
  styles: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.array, _propTypes.default.func, _propTypes.default.number, _propTypes.default.object, _propTypes.default.string, _propTypes.default.bool])
} : void 0;
var _default = exports.default = GlobalStyles;