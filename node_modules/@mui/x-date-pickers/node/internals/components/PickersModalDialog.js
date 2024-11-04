"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.PickersModalDialog = PickersModalDialog;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var React = _interopRequireWildcard(require("react"));
var _DialogContent = _interopRequireDefault(require("@mui/material/DialogContent"));
var _Fade = _interopRequireDefault(require("@mui/material/Fade"));
var _Dialog = _interopRequireWildcard(require("@mui/material/Dialog"));
var _styles = require("@mui/material/styles");
var _dimensions = require("../constants/dimensions");
var _jsxRuntime = require("react/jsx-runtime");
const PickersModalDialogRoot = (0, _styles.styled)(_Dialog.default)({
  [`& .${_Dialog.dialogClasses.container}`]: {
    outline: 0
  },
  [`& .${_Dialog.dialogClasses.paper}`]: {
    outline: 0,
    minWidth: _dimensions.DIALOG_WIDTH
  }
});
const PickersModalDialogContent = (0, _styles.styled)(_DialogContent.default)({
  '&:first-of-type': {
    padding: 0
  }
});
function PickersModalDialog(props) {
  const {
    children,
    onDismiss,
    open,
    slots,
    slotProps
  } = props;
  const Dialog = slots?.dialog ?? PickersModalDialogRoot;
  const Transition = slots?.mobileTransition ?? _Fade.default;
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(Dialog, (0, _extends2.default)({
    open: open,
    onClose: onDismiss
  }, slotProps?.dialog, {
    TransitionComponent: Transition,
    TransitionProps: slotProps?.mobileTransition,
    PaperComponent: slots?.mobilePaper,
    PaperProps: slotProps?.mobilePaper,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(PickersModalDialogContent, {
      children: children
    })
  }));
}