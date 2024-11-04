"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useDesktopPicker = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var _objectWithoutPropertiesLoose2 = _interopRequireDefault(require("@babel/runtime/helpers/objectWithoutPropertiesLoose"));
var React = _interopRequireWildcard(require("react"));
var _useSlotProps3 = _interopRequireDefault(require("@mui/utils/useSlotProps"));
var _InputAdornment = _interopRequireDefault(require("@mui/material/InputAdornment"));
var _IconButton = _interopRequireDefault(require("@mui/material/IconButton"));
var _useForkRef = _interopRequireDefault(require("@mui/utils/useForkRef"));
var _useId = _interopRequireDefault(require("@mui/utils/useId"));
var _PickersPopper = require("../../components/PickersPopper");
var _usePicker = require("../usePicker");
var _PickersLayout = require("../../../PickersLayout");
var _PickersProvider = require("../../components/PickersProvider");
var _jsxRuntime = require("react/jsx-runtime");
const _excluded = ["props", "getOpenDialogAriaText"],
  _excluded2 = ["ownerState"],
  _excluded3 = ["ownerState"];
/**
 * Hook managing all the single-date desktop pickers:
 * - DesktopDatePicker
 * - DesktopDateTimePicker
 * - DesktopTimePicker
 */
const useDesktopPicker = _ref => {
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
    autoFocus,
    localeText,
    reduceAnimations
  } = props;
  const containerRef = React.useRef(null);
  const fieldRef = React.useRef(null);
  const labelId = (0, _useId.default)();
  const isToolbarHidden = innerSlotProps?.toolbar?.hidden ?? false;
  const {
    open,
    actions,
    hasUIView,
    layoutProps,
    renderCurrentView,
    shouldRestoreFocus,
    fieldProps: pickerFieldProps,
    contextValue,
    ownerState
  } = (0, _usePicker.usePicker)((0, _extends2.default)({}, pickerParams, {
    props,
    fieldRef,
    autoFocusView: true,
    additionalViewProps: {},
    wrapperVariant: 'desktop'
  }));
  const InputAdornment = slots.inputAdornment ?? _InputAdornment.default;
  const _useSlotProps = (0, _useSlotProps3.default)({
      elementType: InputAdornment,
      externalSlotProps: innerSlotProps?.inputAdornment,
      additionalProps: {
        position: 'end'
      },
      ownerState: props
    }),
    inputAdornmentProps = (0, _objectWithoutPropertiesLoose2.default)(_useSlotProps, _excluded2);
  const OpenPickerButton = slots.openPickerButton ?? _IconButton.default;
  const _useSlotProps2 = (0, _useSlotProps3.default)({
      elementType: OpenPickerButton,
      externalSlotProps: innerSlotProps?.openPickerButton,
      additionalProps: {
        disabled: disabled || readOnly,
        onClick: open ? actions.onClose : actions.onOpen,
        'aria-label': getOpenDialogAriaText(pickerFieldProps.value),
        edge: inputAdornmentProps.position
      },
      ownerState: props
    }),
    openPickerButtonProps = (0, _objectWithoutPropertiesLoose2.default)(_useSlotProps2, _excluded3);
  const OpenPickerIcon = slots.openPickerIcon;
  const openPickerIconProps = (0, _useSlotProps3.default)({
    elementType: OpenPickerIcon,
    externalSlotProps: innerSlotProps?.openPickerIcon,
    ownerState
  });
  const Field = slots.field;
  const fieldProps = (0, _useSlotProps3.default)({
    elementType: Field,
    externalSlotProps: innerSlotProps?.field,
    additionalProps: (0, _extends2.default)({}, pickerFieldProps, isToolbarHidden && {
      id: labelId
    }, {
      readOnly,
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
      name,
      autoFocus: autoFocus && !props.open,
      focused: open ? true : undefined
    }, inputRef ? {
      inputRef
    } : {}),
    ownerState: props
  });

  // TODO: Move to `useSlotProps` when https://github.com/mui/material-ui/pull/35088 will be merged
  if (hasUIView) {
    fieldProps.InputProps = (0, _extends2.default)({}, fieldProps.InputProps, {
      ref: containerRef
    }, !props.disableOpenPicker && {
      [`${inputAdornmentProps.position}Adornment`]: /*#__PURE__*/(0, _jsxRuntime.jsx)(InputAdornment, (0, _extends2.default)({}, inputAdornmentProps, {
        children: /*#__PURE__*/(0, _jsxRuntime.jsx)(OpenPickerButton, (0, _extends2.default)({}, openPickerButtonProps, {
          children: /*#__PURE__*/(0, _jsxRuntime.jsx)(OpenPickerIcon, (0, _extends2.default)({}, openPickerIconProps))
        }))
      }))
    });
  }
  const slotsForField = (0, _extends2.default)({
    textField: slots.textField,
    clearIcon: slots.clearIcon,
    clearButton: slots.clearButton
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
    popper: (0, _extends2.default)({
      'aria-labelledby': labelledById
    }, innerSlotProps?.popper)
  });
  const handleFieldRef = (0, _useForkRef.default)(fieldRef, fieldProps.unstableFieldRef);
  const renderPicker = () => /*#__PURE__*/(0, _jsxRuntime.jsxs)(_PickersProvider.PickersProvider, {
    contextValue: contextValue,
    localeText: localeText,
    children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(Field, (0, _extends2.default)({}, fieldProps, {
      slots: slotsForField,
      slotProps: slotProps,
      unstableFieldRef: handleFieldRef
    })), /*#__PURE__*/(0, _jsxRuntime.jsx)(_PickersPopper.PickersPopper, (0, _extends2.default)({
      role: "dialog",
      placement: "bottom-start",
      anchorEl: containerRef.current
    }, actions, {
      open: open,
      slots: slots,
      slotProps: slotProps,
      shouldRestoreFocus: shouldRestoreFocus,
      reduceAnimations: reduceAnimations,
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
exports.useDesktopPicker = useDesktopPicker;