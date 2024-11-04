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
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _Typography = _interopRequireDefault(require("../Typography"));
var _alertTitleClasses = require("./alertTitleClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes
  } = ownerState;
  const slots = {
    root: ['root']
  };
  return (0, _composeClasses.default)(slots, _alertTitleClasses.getAlertTitleUtilityClass, classes);
};
const AlertTitleRoot = (0, _zeroStyled.styled)(_Typography.default, {
  name: 'MuiAlertTitle',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})((0, _memoTheme.default)(({
  theme
}) => {
  return {
    fontWeight: theme.typography.fontWeightMedium,
    marginTop: -2
  };
}));
const AlertTitle = /*#__PURE__*/React.forwardRef(function AlertTitle(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiAlertTitle'
  });
  const {
    className,
    ...other
  } = props;
  const ownerState = props;
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(AlertTitleRoot, {
    gutterBottom: true,
    component: "div",
    ownerState: ownerState,
    ref: ref,
    className: (0, _clsx.default)(classes.root, className),
    ...other
  });
});
process.env.NODE_ENV !== "production" ? AlertTitle.propTypes /* remove-proptypes */ = {
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
var _default = exports.default = AlertTitle;