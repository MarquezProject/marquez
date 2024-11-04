import * as React from 'react';
export function usePickerOwnerState(parameters) {
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