"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
const usePreviousProps = value => {
  const ref = React.useRef({});
  React.useEffect(() => {
    ref.current = value;
  });
  return ref.current;
};
var _default = exports.default = usePreviousProps;