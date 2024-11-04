"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.usePickerOwnerState = usePickerOwnerState;
var React = _interopRequireWildcard(require("react"));
function usePickerOwnerState(parameters) {
  const {
    props,
    pickerValueResponse
  } = parameters;
  return React.useMemo(() => ({
    value: pickerValueResponse.viewProps.value,
    open: pickerValueResponse.open,
    disabled: props.disabled ?? false,
    readOnly: props.readOnly ?? false
  }), [pickerValueResponse.viewProps.value, pickerValueResponse.open, props.disabled, props.readOnly]);
}