"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
/**
 * @ignore - internal component.
 */
const ListContext = /*#__PURE__*/React.createContext({});
if (process.env.NODE_ENV !== 'production') {
  ListContext.displayName = 'ListContext';
}
var _default = exports.default = ListContext;