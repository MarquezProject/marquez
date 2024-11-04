"use strict";
'use client';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useTimeField = void 0;
var _valueManagers = require("../internals/utils/valueManagers");
var _useField = require("../internals/hooks/useField");
var _validation = require("../validation");
var _hooks = require("../hooks");
var _defaultizedFieldProps = require("../internals/hooks/defaultizedFieldProps");
const useTimeField = inProps => {
  const props = (0, _defaultizedFieldProps.useDefaultizedTimeField)(inProps);
  const {
    forwardedProps,
    internalProps
  } = (0, _hooks.useSplitFieldProps)(props, 'time');
  return (0, _useField.useField)({
    forwardedProps,
    internalProps,
    valueManager: _valueManagers.singleItemValueManager,
    fieldValueManager: _valueManagers.singleItemFieldValueManager,
    validator: _validation.validateTime,
    valueType: 'time'
  });
};
exports.useTimeField = useTimeField;