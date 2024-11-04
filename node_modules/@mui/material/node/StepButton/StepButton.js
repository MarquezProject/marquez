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
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _ButtonBase = _interopRequireDefault(require("../ButtonBase"));
var _StepLabel = _interopRequireDefault(require("../StepLabel"));
var _isMuiElement = _interopRequireDefault(require("../utils/isMuiElement"));
var _StepperContext = _interopRequireDefault(require("../Stepper/StepperContext"));
var _StepContext = _interopRequireDefault(require("../Step/StepContext"));
var _stepButtonClasses = _interopRequireWildcard(require("./stepButtonClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    orientation
  } = ownerState;
  const slots = {
    root: ['root', orientation],
    touchRipple: ['touchRipple']
  };
  return (0, _composeClasses.default)(slots, _stepButtonClasses.getStepButtonUtilityClass, classes);
};
const StepButtonRoot = (0, _zeroStyled.styled)(_ButtonBase.default, {
  name: 'MuiStepButton',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [{
      [`& .${_stepButtonClasses.default.touchRipple}`]: styles.touchRipple
    }, styles.root, styles[ownerState.orientation]];
  }
})({
  width: '100%',
  padding: '24px 16px',
  margin: '-24px -16px',
  boxSizing: 'content-box',
  [`& .${_stepButtonClasses.default.touchRipple}`]: {
    color: 'rgba(0, 0, 0, 0.3)'
  },
  variants: [{
    props: {
      orientation: 'vertical'
    },
    style: {
      justifyContent: 'flex-start',
      padding: '8px',
      margin: '-8px'
    }
  }]
});
const StepButton = /*#__PURE__*/React.forwardRef(function StepButton(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiStepButton'
  });
  const {
    children,
    className,
    icon,
    optional,
    ...other
  } = props;
  const {
    disabled,
    active
  } = React.useContext(_StepContext.default);
  const {
    orientation
  } = React.useContext(_StepperContext.default);
  const ownerState = {
    ...props,
    orientation
  };
  const classes = useUtilityClasses(ownerState);
  const childProps = {
    icon,
    optional
  };
  const child = (0, _isMuiElement.default)(children, ['StepLabel']) ? (/*#__PURE__*/React.cloneElement(children, childProps)) : /*#__PURE__*/(0, _jsxRuntime.jsx)(_StepLabel.default, {
    ...childProps,
    children: children
  });
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(StepButtonRoot, {
    focusRipple: true,
    disabled: disabled,
    TouchRippleProps: {
      className: classes.touchRipple
    },
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    "aria-current": active ? 'step' : undefined,
    ...other,
    children: child
  });
});
process.env.NODE_ENV !== "production" ? StepButton.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Can be a `StepLabel` or a node to place inside `StepLabel` as children.
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
   * The icon displayed by the step label.
   */
  icon: _propTypes.default.node,
  /**
   * The optional node to display.
   */
  optional: _propTypes.default.node,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = StepButton;