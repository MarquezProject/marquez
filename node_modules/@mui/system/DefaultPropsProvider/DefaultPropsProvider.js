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
var _resolveProps = _interopRequireDefault(require("@mui/utils/resolveProps"));
var _jsxRuntime = require("react/jsx-runtime");
const PropsContext = /*#__PURE__*/React.createContext(undefined);
function DefaultPropsProvider({
  value,
  children
}) {
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(PropsContext.Provider, {
    value: value,
    children: children
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
  value: _propTypes.default.object
} : void 0;
function getThemeProps(params) {
  const {
    theme,
    name,
    props
  } = params;
  if (!theme || !theme.components || !theme.components[name]) {
    return props;
  }
  const config = theme.components[name];
  if (config.defaultProps) {
    // compatible with v5 signature
    return (0, _resolveProps.default)(config.defaultProps, props);
  }
  if (!config.styleOverrides && !config.variants) {
    // v6 signature, no property 'defaultProps'
    return (0, _resolveProps.default)(config, props);
  }
  return props;
}
function useDefaultProps({
  props,
  name
}) {
  const ctx = React.useContext(PropsContext);
  return getThemeProps({
    props,
    name,
    theme: {
      components: ctx
    }
  });
}
var _default = exports.default = DefaultPropsProvider;