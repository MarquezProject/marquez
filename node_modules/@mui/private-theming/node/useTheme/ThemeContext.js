"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
const ThemeContext = /*#__PURE__*/React.createContext(null);
if (process.env.NODE_ENV !== 'production') {
  ThemeContext.displayName = 'ThemeContext';
}
var _default = exports.default = ThemeContext;