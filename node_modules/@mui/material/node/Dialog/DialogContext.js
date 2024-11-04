"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
const DialogContext = /*#__PURE__*/React.createContext({});
if (process.env.NODE_ENV !== 'production') {
  DialogContext.displayName = 'DialogContext';
}
var _default = exports.default = DialogContext;