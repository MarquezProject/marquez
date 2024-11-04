"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.usePicker = void 0;
var _warning = require("@mui/x-internals/warning");
var _usePickerValue = require("./usePickerValue");
var _usePickerViews = require("./usePickerViews");
var _usePickerLayoutProps = require("./usePickerLayoutProps");
var _usePickerOwnerState = require("./usePickerOwnerState");
const usePicker = ({
  props,
  valueManager,
  valueType,
  wrapperVariant,
  additionalViewProps,
  validator,
  autoFocusView,
  rendererInterceptor,
  fieldRef
}) => {
  if (process.env.NODE_ENV !== 'production') {
    if (props.renderInput != null) {
      (0, _warning.warnOnce)(['MUI X: The `renderInput` prop has been removed in version 6.0 of the Date and Time Pickers.', 'You can replace it with the `textField` component slot in most cases.', 'For more information, please have a look at the migration guide (https://mui.com/x/migration/migration-pickers-v5/#input-renderer-required-in-v5).']);
    }
  }
  const pickerValueResponse = (0, _usePickerValue.usePickerValue)({
    props,
    valueManager,
    valueType,
    wrapperVariant,
    validator
  });
  const pickerViewsResponse = (0, _usePickerViews.usePickerViews)({
    props,
    additionalViewProps,
    autoFocusView,
    fieldRef,
    propsFromPickerValue: pickerValueResponse.viewProps,
    rendererInterceptor
  });
  const pickerLayoutResponse = (0, _usePickerLayoutProps.usePickerLayoutProps)({
    props,
    wrapperVariant,
    propsFromPickerValue: pickerValueResponse.layoutProps,
    propsFromPickerViews: pickerViewsResponse.layoutProps
  });
  const pickerOwnerState = (0, _usePickerOwnerState.usePickerOwnerState)({
    props,
    pickerValueResponse
  });
  return {
    // Picker value
    open: pickerValueResponse.open,
    actions: pickerValueResponse.actions,
    fieldProps: pickerValueResponse.fieldProps,
    // Picker views
    renderCurrentView: pickerViewsResponse.renderCurrentView,
    hasUIView: pickerViewsResponse.hasUIView,
    shouldRestoreFocus: pickerViewsResponse.shouldRestoreFocus,
    // Picker layout
    layoutProps: pickerLayoutResponse.layoutProps,
    // Picker context
    contextValue: pickerValueResponse.contextValue,
    // Picker owner state
    ownerState: pickerOwnerState
  };
};
exports.usePicker = usePicker;