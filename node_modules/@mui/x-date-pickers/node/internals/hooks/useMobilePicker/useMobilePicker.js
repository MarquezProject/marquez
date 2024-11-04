"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useMobilePicker = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var _objectWithoutPropertiesLoose2 = _interopRequireDefault(require("@babel/runtime/helpers/objectWithoutPropertiesLoose"));
var React = _interopRequireWildcard(require("react"));
var _useSlotProps = _interopRequireDefault(require("@mui/utils/useSlotProps"));
var _useForkRef = _interopRequireDefault(require("@mui/utils/useForkRef"));
var _useId = _interopRequireDefault(require("@mui/utils/useId"));
var _PickersModalDialog = require("../../components/PickersModalDialog");
var _usePicker = require("../usePicker");
var _utils = require("../../utils/utils");
var _PickersLayout = require("../../../PickersLayout");
var _PickersProvider = require("../../components/PickersProvider");
var _jsxRuntime = require("react/jsx-runtime");
const _excluded = ["props", "getOpenDialogAriaText"];
/**
 * Hook managing all the single-date mobile pickers:
 * - MobileDatePicker
 * - MobileDateTimePicker
 * - MobileTimePicker
 */
const useMobilePicker = _ref => {
  let {
      props,
      getOpenDialogAriaText
    } = _ref,
    pickerParams = (0, _objectWithoutPropertiesLoose2.default)(_ref, _excluded);
  const {
    slots,
    slotProps: innerSlotProps,
    className,
    sx,
    format,
    formatDensity,
    enableAccessibleFieldDOMStructure,
    selectedSections,
    onSelectedSectionsChange,
    timezone,
    name,
    label,
    inputRef,
    readOnly,
    disabled,
    localeText
  } = props;
  const fieldRef = React.useRef(null);
  const labelId = (0, _useId.default)();
  const isToolbarHidden = innerSlotProps?.toolbar?.hidden ?? false;
  const {
    open,
    actions,
    layoutProps,
    renderCurrentView,
    fieldProps: pickerFieldProps,
    contextValue
  } = (0, _usePicker.usePicker)((0, _extends2.default)({}, pickerParams, {
    props,
    fieldRef,
    autoFocusView: true,
    additionalViewProps: {},
    wrapperVariant: 'mobile'
  }));
  const Field = slots.field;
  const fieldProps = (0, _useSlotProps.default)({
    elementType: Field,
    externalSlotProps: innerSlotProps?.field,
    additionalProps: (0, _extends2.default)({}, pickerFieldProps, isToolbarHidden && {
      id: labelId
    }, !(disabled || readOnly) && {
      onClick: actions.onOpen,
      onKeyDown: (0, _utils.onSpaceOrEnter)(actions.onOpen)
    }, {
      readOnly: readOnly ?? true,
      disabled,
      className,
      sx,
      format,
      formatDensity,
      enableAccessibleFieldDOMStructure,
      selectedSections,
      onSelectedSectionsChange,
      timezone,
      label,
      name
    }, inputRef ? {
      inputRef
    } : {}),
    ownerState: props
  });

  // TODO: Move to `useSlotProps` when https://github.com/mui/material-ui/pull/35088 will be merged
  fieldProps.inputProps = (0, _extends2.default)({}, fieldProps.inputProps, {
    'aria-label': getOpenDialogAriaText(pickerFieldProps.value)
  });
  const slotsForField = (0, _extends2.default)({
    textField: slots.textField
  }, fieldProps.slots);
  const Layout = slots.layout ?? _PickersLayout.PickersLayout;
  let labelledById = labelId;
  if (isToolbarHidden) {
    if (label) {
      labelledById = `${labelId}-label`;
    } else {
      labelledById = undefined;
    }
  }
  const slotProps = (0, _extends2.default)({}, innerSlotProps, {
    toolbar: (0, _extends2.default)({}, innerSlotProps?.toolbar, {
      titleId: labelId
    }),
    mobilePaper: (0, _extends2.default)({
      'aria-labelledby': labelledById
    }, innerSlotProps?.mobilePaper)
  });
  const handleFieldRef = (0, _useForkRef.default)(fieldRef, fieldProps.unstableFieldRef);
  const renderPicker = () => /*#__PURE__*/(0, _jsxRuntime.jsxs)(_PickersProvider.PickersProvider, {
    contextValue: contextValue,
    localeText: localeText,
    children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(Field, (0, _extends2.default)({}, fieldProps, {
      slots: slotsForField,
      slotProps: slotProps,
      unstableFieldRef: handleFieldRef
    })), /*#__PURE__*/(0, _jsxRuntime.jsx)(_PickersModalDialog.PickersModalDialog, (0, _extends2.default)({}, actions, {
      open: open,
      slots: slots,
      slotProps: slotProps,
      children: /*#__PURE__*/(0, _jsxRuntime.jsx)(Layout, (0, _extends2.default)({}, layoutProps, slotProps?.layout, {
        slots: slots,
        slotProps: slotProps,
        children: renderCurrentView()
      }))
    }))]
  });
  return {
    renderPicker
  };
};
exports.useMobilePicker = useMobilePicker;