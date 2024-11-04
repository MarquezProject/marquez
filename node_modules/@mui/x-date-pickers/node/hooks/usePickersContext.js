"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.usePickersContext = void 0;
var React = _interopRequireWildcard(require("react"));
var _PickersProvider = require("../internals/components/PickersProvider");
/**
 * Returns the context passed by the picker that wraps the current component.
 */
const usePickersContext = () => {
  const value = React.useContext(_PickersProvider.PickersContext);
  if (value == null) {
    throw new Error(['MUI X: The `usePickersContext` can only be called in fields that are used as a slot of a picker component'].join('\n'));
  }
  return value;
};
exports.usePickersContext = usePickersContext;