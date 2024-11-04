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
var _styledEngine = require("@mui/styled-engine");
var _useTheme = _interopRequireDefault(require("../useTheme"));
var _jsxRuntime = require("react/jsx-runtime");
function GlobalStyles({
  styles,
  themeId,
  defaultTheme = {}
}) {
  const upperTheme = (0, _useTheme.default)(defaultTheme);
  const globalStyles = typeof styles === 'function' ? styles(themeId ? upperTheme[themeId] || upperTheme : upperTheme) : styles;
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_styledEngine.GlobalStyles, {
    styles: globalStyles
  });
}
process.env.NODE_ENV !== "production" ? GlobalStyles.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │ To update them, edit the TypeScript types and run `pnpm proptypes`. │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * @ignore
   */
  defaultTheme: _propTypes.default.object,
  /**
   * @ignore
   */
  styles: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.array, _propTypes.default.func, _propTypes.default.number, _propTypes.default.object, _propTypes.default.string, _propTypes.default.bool]),
  /**
   * @ignore
   */
  themeId: _propTypes.default.string
} : void 0;
var _default = exports.default = GlobalStyles;