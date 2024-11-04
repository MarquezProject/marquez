"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = useRadioGroup;
var React = _interopRequireWildcard(require("react"));
var _RadioGroupContext = _interopRequireDefault(require("./RadioGroupContext"));
function useRadioGroup() {
  return React.useContext(_RadioGroupContext.default);
}