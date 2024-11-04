import _extends from "@babel/runtime/helpers/esm/extends";
import _objectWithoutPropertiesLoose from "@babel/runtime/helpers/esm/objectWithoutPropertiesLoose";
const _excluded = ["props", "getOpenDialogAriaText"];
import * as React from 'react';
import useSlotProps from '@mui/utils/useSlotProps';
import useForkRef from '@mui/utils/useForkRef';
import useId from '@mui/utils/useId';
import { PickersModalDialog } from "../../components/PickersModalDialog.js";
import { usePicker } from "../usePicker/index.js";
import { onSpaceOrEnter } from "../../utils/utils.js";
import { PickersLayout } from "../../../PickersLayout/index.js";
import { PickersProvider } from "../../components/PickersProvider.js";

/**
 * Hook managing all the single-date mobile pickers:
 * - MobileDatePicker
 * - MobileDateTimePicker
 * - MobileTimePicker
 */
import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
export const useMobilePicker = _ref => {
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
    localeText
  } = props;
  const fieldRef = React.useRef(null);
  const labelId = useId();
  const isToolbarHidden = innerSlotProps?.toolbar?.hidden ?? false;
  const {
    open,
    actions,
    layoutProps,
    renderCurrentView,
    fieldProps: pickerFieldProps,
    contextValue
  } = usePicker(_extends({}, pickerParams, {
    props,
    fieldRef,
    autoFocusView: true,
    additionalViewProps: {},
    wrapperVariant: 'mobile'
  }));
  const Field = slots.field;
  const fieldProps = useSlotProps({
    elementType: Field,
    externalSlotProps: innerSlotProps?.field,
    additionalProps: _extends({}, pickerFieldProps, isToolbarHidden && {
      id: labelId
    }, !(disabled || readOnly) && {
      onClick: actions.onOpen,
      onKeyDown: onSpaceOrEnter(actions.onOpen)
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
  fieldProps.inputProps = _extends({}, fieldProps.inputProps, {
    'aria-label': getOpenDialogAriaText(pickerFieldProps.value)
  });
  const slotsForField = _extends({
    textField: slots.textField
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
    mobilePaper: _extends({
      'aria-labelledby': labelledById
    }, innerSlotProps?.mobilePaper)
  });
  const handleFieldRef = useForkRef(fieldRef, fieldProps.unstableFieldRef);
  const renderPicker = () => /*#__PURE__*/_jsxs(PickersProvider, {
    contextValue: contextValue,
    localeText: localeText,
    children: [/*#__PURE__*/_jsx(Field, _extends({}, fieldProps, {
      slots: slotsForField,
      slotProps: slotProps,
      unstableFieldRef: handleFieldRef
    })), /*#__PURE__*/_jsx(PickersModalDialog, _extends({}, actions, {
      open: open,
      slots: slots,
      slotProps: slotProps,
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