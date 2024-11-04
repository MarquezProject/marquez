"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var React = _interopRequireWildcard(require("react"));
var _useSlotProps = _interopRequireDefault(require("@mui/utils/useSlotProps"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _PickersActionBar = require("../PickersActionBar");
var _pickersLayoutClasses = require("./pickersLayoutClasses");
var _PickersShortcuts = require("../PickersShortcuts");
var _jsxRuntime = require("react/jsx-runtime");
function toolbarHasView(toolbarProps) {
  return toolbarProps.view !== null;
}
const useUtilityClasses = ownerState => {
  const {
    classes,
    isLandscape
  } = ownerState;
  const slots = {
    root: ['root', isLandscape && 'landscape'],
    contentWrapper: ['contentWrapper'],
    toolbar: ['toolbar'],
    actionBar: ['actionBar'],
    tabs: ['tabs'],
    landscape: ['landscape'],
    shortcuts: ['shortcuts']
  };
  return (0, _composeClasses.default)(slots, _pickersLayoutClasses.getPickersLayoutUtilityClass, classes);
};
const usePickerLayout = props => {
  const {
    wrapperVariant,
    onAccept,
    onClear,
    onCancel,
    onSetToday,
    view,
    views,
    onViewChange,
    value,
    onChange,
    onSelectShortcut,
    isValid,
    isLandscape,
    disabled,
    readOnly,
    children,
    slots,
    slotProps
    // TODO: Remove this "as" hack. It get introduced to mark `value` prop in PickersLayoutProps as not required.
    // The true type should be
    // - For pickers value: TDate | null
    // - For range pickers value: [TDate | null, TDate | null]
  } = props;
  const classes = useUtilityClasses(props);

  // Action bar
  const ActionBar = slots?.actionBar ?? _PickersActionBar.PickersActionBar;
  const actionBarProps = (0, _useSlotProps.default)({
    elementType: ActionBar,
    externalSlotProps: slotProps?.actionBar,
    additionalProps: {
      onAccept,
      onClear,
      onCancel,
      onSetToday,
      actions: wrapperVariant === 'desktop' ? [] : ['cancel', 'accept']
    },
    className: classes.actionBar,
    ownerState: (0, _extends2.default)({}, props, {
      wrapperVariant
    })
  });
  const actionBar = /*#__PURE__*/(0, _jsxRuntime.jsx)(ActionBar, (0, _extends2.default)({}, actionBarProps));

  // Toolbar
  const Toolbar = slots?.toolbar;
  const toolbarProps = (0, _useSlotProps.default)({
    elementType: Toolbar,
    externalSlotProps: slotProps?.toolbar,
    additionalProps: {
      isLandscape,
      onChange,
      value,
      view,
      onViewChange,
      views,
      disabled,
      readOnly
    },
    className: classes.toolbar,
    ownerState: (0, _extends2.default)({}, props, {
      wrapperVariant
    })
  });
  const toolbar = toolbarHasView(toolbarProps) && !!Toolbar ? /*#__PURE__*/(0, _jsxRuntime.jsx)(Toolbar, (0, _extends2.default)({}, toolbarProps)) : null;

  // Content
  const content = children;

  // Tabs
  const Tabs = slots?.tabs;
  const tabs = view && Tabs ? /*#__PURE__*/(0, _jsxRuntime.jsx)(Tabs, (0, _extends2.default)({
    view: view,
    onViewChange: onViewChange,
    className: classes.tabs
  }, slotProps?.tabs)) : null;

  // Shortcuts
  const Shortcuts = slots?.shortcuts ?? _PickersShortcuts.PickersShortcuts;
  const shortcutsProps = (0, _useSlotProps.default)({
    elementType: Shortcuts,
    externalSlotProps: slotProps?.shortcuts,
    additionalProps: {
      isValid,
      isLandscape,
      onChange: onSelectShortcut
    },
    className: classes.shortcuts,
    ownerState: {
      isValid,
      isLandscape,
      onChange: onSelectShortcut,
      wrapperVariant
    }
  });
  const shortcuts = view && !!Shortcuts ? /*#__PURE__*/(0, _jsxRuntime.jsx)(Shortcuts, (0, _extends2.default)({}, shortcutsProps)) : null;
  return {
    toolbar,
    content,
    tabs,
    actionBar,
    shortcuts
  };
};
var _default = exports.default = usePickerLayout;