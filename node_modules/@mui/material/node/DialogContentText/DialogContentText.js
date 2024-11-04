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
var _rootShouldForwardProp = _interopRequireDefault(require("../styles/rootShouldForwardProp"));
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _Typography = _interopRequireDefault(require("../Typography"));
var _dialogContentTextClasses = require("./dialogContentTextClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes
  } = ownerState;
  const slots = {
    root: ['root']
  };
  const composedClasses = (0, _composeClasses.default)(slots, _dialogContentTextClasses.getDialogContentTextUtilityClass, classes);
  return {
    ...classes,
    // forward classes to the Typography
    ...composedClasses
  };
};
const DialogContentTextRoot = (0, _zeroStyled.styled)(_Typography.default, {
  shouldForwardProp: prop => (0, _rootShouldForwardProp.default)(prop) || prop === 'classes',
  name: 'MuiDialogContentText',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})({});
const DialogContentText = /*#__PURE__*/React.forwardRef(function DialogContentText(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiDialogContentText'
  });
  const {
    children,
    className,
    ...ownerState
  } = props;
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(DialogContentTextRoot, {
    component: "p",
    variant: "body1",
    color: "textSecondary",
    ref: ref,
    ownerState: ownerState,
    className: (0, _clsx.default)(classes.root, className),
    ...props,
    classes: classes
  });
});
process.env.NODE_ENV !== "production" ? DialogContentText.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
   */
  children: _propTypes.default.node,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = DialogContentText;