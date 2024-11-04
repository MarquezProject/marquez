"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
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
var _CheckCircle = _interopRequireDefault(require("../internal/svg-icons/CheckCircle"));
var _Warning = _interopRequireDefault(require("../internal/svg-icons/Warning"));
var _SvgIcon = _interopRequireDefault(require("../SvgIcon"));
var _stepIconClasses = _interopRequireWildcard(require("./stepIconClasses"));
var _jsxRuntime = require("react/jsx-runtime");
var _circle;
const useUtilityClasses = ownerState => {
  const {
    classes,
    active,
    completed,
    error
  } = ownerState;
  const slots = {
    root: ['root', active && 'active', completed && 'completed', error && 'error'],
    text: ['text']
  };
  return (0, _composeClasses.default)(slots, _stepIconClasses.getStepIconUtilityClass, classes);
};
const StepIconRoot = (0, _zeroStyled.styled)(_SvgIcon.default, {
  name: 'MuiStepIcon',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'block',
  transition: theme.transitions.create('color', {
    duration: theme.transitions.duration.shortest
  }),
  color: (theme.vars || theme).palette.text.disabled,
  [`&.${_stepIconClasses.default.completed}`]: {
    color: (theme.vars || theme).palette.primary.main
  },
  [`&.${_stepIconClasses.default.active}`]: {
    color: (theme.vars || theme).palette.primary.main
  },
  [`&.${_stepIconClasses.default.error}`]: {
    color: (theme.vars || theme).palette.error.main
  }
})));
const StepIconText = (0, _zeroStyled.styled)('text', {
  name: 'MuiStepIcon',
  slot: 'Text',
  overridesResolver: (props, styles) => styles.text
})((0, _memoTheme.default)(({
  theme
}) => ({
  fill: (theme.vars || theme).palette.primary.contrastText,
  fontSize: theme.typography.caption.fontSize,
  fontFamily: theme.typography.fontFamily
})));
const StepIcon = /*#__PURE__*/React.forwardRef(function StepIcon(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiStepIcon'
  });
  const {
    active = false,
    className: classNameProp,
    completed = false,
    error = false,
    icon,
    ...other
  } = props;
  const ownerState = {
    ...props,
    active,
    completed,
    error
  };
  const classes = useUtilityClasses(ownerState);
  if (typeof icon === 'number' || typeof icon === 'string') {
    const className = (0, _clsx.default)(classNameProp, classes.root);
    if (error) {
      return /*#__PURE__*/(0, _jsxRuntime.jsx)(StepIconRoot, {
        as: _Warning.default,
        className: className,
        ref: ref,
        ownerState: ownerState,
        ...other
      });
    }
    if (completed) {
      return /*#__PURE__*/(0, _jsxRuntime.jsx)(StepIconRoot, {
        as: _CheckCircle.default,
        className: className,
        ref: ref,
        ownerState: ownerState,
        ...other
      });
    }
    return /*#__PURE__*/(0, _jsxRuntime.jsxs)(StepIconRoot, {
      className: className,
      ref: ref,
      ownerState: ownerState,
      ...other,
      children: [_circle || (_circle = /*#__PURE__*/(0, _jsxRuntime.jsx)("circle", {
        cx: "12",
        cy: "12",
        r: "12"
      })), /*#__PURE__*/(0, _jsxRuntime.jsx)(StepIconText, {
        className: classes.text,
        x: "12",
        y: "12",
        textAnchor: "middle",
        dominantBaseline: "central",
        ownerState: ownerState,
        children: icon
      })]
    });
  }
  return icon;
});
process.env.NODE_ENV !== "production" ? StepIcon.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Whether this step is active.
   * @default false
   */
  active: _propTypes.default.bool,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * Mark the step as completed. Is passed to child components.
   * @default false
   */
  completed: _propTypes.default.bool,
  /**
   * If `true`, the step is marked as failed.
   * @default false
   */
  error: _propTypes.default.bool,
  /**
   * The label displayed in the step icon.
   */
  icon: _propTypes.default.node,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = StepIcon;