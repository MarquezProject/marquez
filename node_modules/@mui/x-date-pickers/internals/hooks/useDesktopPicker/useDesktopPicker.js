import _extends from "@babel/runtime/helpers/esm/extends";
import _objectWithoutPropertiesLoose from "@babel/runtime/helpers/esm/objectWithoutPropertiesLoose";
const _excluded = ["props", "getOpenDialogAriaText"],
  _excluded2 = ["ownerState"],
  _excluded3 = ["ownerState"];
import * as React from 'react';
import useSlotProps from '@mui/utils/useSlotProps';
import MuiInputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import useForkRef from '@mui/utils/useForkRef';
import useId from '@mui/utils/useId';
import { PickersPopper } from "../../components/PickersPopper.js";
import { usePicker } from "../usePicker/index.js";
import { PickersLayout } from "../../../PickersLayout/index.js";
import { PickersProvider } from "../../components/PickersProvider.js";

/**
 * Hook managing all the single-date desktop pickers:
 * - DesktopDatePicker
 * - DesktopDateTimePicker
 * - DesktopTimePicker
 */
import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
export const useDesktopPicker = _ref => {
  let {
      props,
      getOpenDialogAriaText
    } = _ref,
    pickerParams = _objectWithoutPropertiesLoose(_ref, _excluded);
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
  const labelId = useId();
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
  } = usePicker(_extends({}, pickerParams, {
    props,
    fieldRef,
    autoFocusView: true,
    additionalViewProps: {},
    wrapperVariant: 'desktop'
  }));
  const InputAdornment = slots.inputAdornment ?? MuiInputAdornment;
  const _useSlotProps = useSlotProps({
      elementType: InputAdornment,
      externalSlotProps: innerSlotProps?.inputAdornment,
      additionalProps: {
        position: 'end'
      },
      ownerState: props
    }),
    inputAdornmentProps = _objectWithoutPropertiesLoose(_useSlotProps, _excluded2);
  const OpenPickerButton = slots.openPickerButton ?? IconButton;
  const _useSlotProps2 = useSlotProps({
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
    openPickerButtonProps = _objectWithoutPropertiesLoose(_useSlotProps2, _excluded3);
  const OpenPickerIcon = slots.openPickerIcon;
  const openPickerIconProps = useSlotProps({
    elementType: OpenPickerIcon,
    externalSlotProps: innerSlotProps?.openPickerIcon,
    ownerState
  });
  const Field = slots.field;
  const fieldProps = useSlotProps({
    elementType: Field,
    externalSlotProps: innerSlotProps?.field,
    additionalProps: _extends({}, pickerFieldProps, isToolbarHidden && {
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
    fieldProps.InputProps = _extends({}, fieldProps.InputProps, {
      ref: containerRef
    }, !props.disableOpenPicker && {
      [`${inputAdornmentProps.position}Adornment`]: /*#__PURE__*/_jsx(InputAdornment, _extends({}, inputAdornmentProps, {
        children: /*#__PURE__*/_jsx(OpenPickerButton, _extends({}, openPickerButtonProps, {
          children: /*#__PURE__*/_jsx(OpenPickerIcon, _extends({}, openPickerIconProps))
        }))
      }))
    });
  }
  const slotsForField = _extends({
    textField: slots.textField,
    clearIcon: slots.clearIcon,
    clearButton: slots.clearButton
  }, fieldProps.slots);
  const Layout = slots.layout ?? PickersLayout;
  let labelledById = labelId;
  if (isToolbarHidden) {
    if (label) {
      labelledById = `${labelId}-label`;
    } else {
      labelledById = undefined;
    }
  }
  const slotProps = _extends({}, innerSlotProps, {
    toolbar: _extends({}, innerSlotProps?.toolbar, {
      titleId: labelId
    }),
    popper: _extends({
      'aria-labelledby': labelledById
    }, innerSlotProps?.popper)
  });
  const handleFieldRef = useForkRef(fieldRef, fieldProps.unstableFieldRef);
  const renderPicker = () => /*#__PURE__*/_jsxs(PickersProvider, {
    contextValue: contextValue,
    localeText: localeText,
    children: [/*#__PURE__*/_jsx(Field, _extends({}, fieldProps, {
      slots: slotsForField,
      slotProps: slotProps,
      unstableFieldRef: handleFieldRef
    })), /*#__PURE__*/_jsx(PickersPopper, _extends({
      role: "dialog",
      placement: "bottom-start",
      anchorEl: containerRef.current
    }, actions, {
      open: open,
      slots: slots,
      slotProps: slotProps,
      shouldRestoreFocus: shouldRestoreFocus,
      reduceAnimations: reduceAnimations,
      children: /*#__PURE__*/_jsx(Layout, _extends({}, layoutProps, slotProps?.layout, {
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