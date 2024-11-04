"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useRtl = exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _jsxRuntime = require("react/jsx-runtime");
const RtlContext = /*#__PURE__*/React.createContext();
function RtlProvider({
  value,
  ...props
}) {
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(RtlContext.Provider, {
    value: value ?? true,
    ...props
  });
}
process.env.NODE_ENV !== "production" ? RtlProvider.propTypes = {
  children: _propTypes.default.node,
  value: _propTypes.default.bool
} : void 0;
const useRtl = () => {
  const value = React.useContext(RtlContext);
  return value ?? false;
};
exports.useRtl = useRtl;
var _default = exports.default = RtlProvider;