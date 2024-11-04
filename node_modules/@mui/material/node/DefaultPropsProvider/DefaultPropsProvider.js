"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
exports.useDefaultProps = useDefaultProps;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _DefaultPropsProvider = _interopRequireWildcard(require("@mui/system/DefaultPropsProvider"));
var _jsxRuntime = require("react/jsx-runtime");
function DefaultPropsProvider(props) {
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_DefaultPropsProvider.default, {
    ...props
  });
}
process.env.NODE_ENV !== "production" ? DefaultPropsProvider.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │ To update them, edit the TypeScript types and run `pnpm proptypes`. │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * @ignore
   */
  children: _propTypes.default.node,
  /**
   * @ignore
   */
  value: _propTypes.default.object.isRequired
} : void 0;
var _default = exports.default = DefaultPropsProvider;
function useDefaultProps(params) {
  return (0, _DefaultPropsProvider.useDefaultProps)(params);
}