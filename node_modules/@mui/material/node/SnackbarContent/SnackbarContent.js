"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _colorManipulator = require("@mui/system/colorManipulator");
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _Paper = _interopRequireDefault(require("../Paper"));
var _snackbarContentClasses = require("./snackbarContentClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes
  } = ownerState;
  const slots = {
    root: ['root'],
    action: ['action'],
    message: ['message']
  };
  return (0, _composeClasses.default)(slots, _snackbarContentClasses.getSnackbarContentUtilityClass, classes);
};
const SnackbarContentRoot = (0, _zeroStyled.styled)(_Paper.default, {
  name: 'MuiSnackbarContent',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})((0, _memoTheme.default)(({
  theme
}) => {
  const emphasis = theme.palette.mode === 'light' ? 0.8 : 0.98;
  const backgroundColor = (0, _colorManipulator.emphasize)(theme.palette.background.default, emphasis);
  return {
    ...theme.typography.body2,
    color: theme.vars ? theme.vars.palette.SnackbarContent.color : theme.palette.getContrastText(backgroundColor),
    backgroundColor: theme.vars ? theme.vars.palette.SnackbarContent.bg : backgroundColor,
    display: 'flex',
    alignItems: 'center',
    flexWrap: 'wrap',
    padding: '6px 16px',
    borderRadius: (theme.vars || theme).shape.borderRadius,
    flexGrow: 1,
    [theme.breakpoints.up('sm')]: {
      flexGrow: 'initial',
      minWidth: 288
    }
  };
}));
const SnackbarContentMessage = (0, _zeroStyled.styled)('div', {
  name: 'MuiSnackbarContent',
  slot: 'Message',
  overridesResolver: (props, styles) => styles.message
})({
  padding: '8px 0'
});
const SnackbarContentAction = (0, _zeroStyled.styled)('div', {
  name: 'MuiSnackbarContent',
  slot: 'Action',
  overridesResolver: (props, styles) => styles.action
})({
  display: 'flex',
  alignItems: 'center',
  marginLeft: 'auto',
  paddingLeft: 16,
  marginRight: -8
});
const SnackbarContent = /*#__PURE__*/React.forwardRef(function SnackbarContent(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiSnackbarContent'
  });
  const {
    action,
    className,
    message,
    role = 'alert',
    ...other
  } = props;
  const ownerState = props;
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(SnackbarContentRoot, {
    role: role,
    square: true,
    elevation: 6,
    className: (0, _clsx.default)(classes.root, className),
    ownerState: ownerState,
    ref: ref,
    ...other,
    children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(SnackbarContentMessage, {
      className: classes.message,
      ownerState: ownerState,
      children: message
    }), action ? /*#__PURE__*/(0, _jsxRuntime.jsx)(SnackbarContentAction, {
      className: classes.action,
      ownerState: ownerState,
      children: action
    }) : null]
  });
});
process.env.NODE_ENV !== "production" ? SnackbarContent.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The action to display. It renders after the message, at the end of the snackbar.
   */
  action: _propTypes.default.node,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The message to display.
   */
  message: _propTypes.default.node,
  /**
   * The ARIA role attribute of the element.
   * @default 'alert'
   */
  role: _propTypes.default /* @typescript-to-proptypes-ignore */.string,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = SnackbarContent;