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
var _integerPropType = _interopRequireDefault(require("@mui/utils/integerPropType"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _stepperClasses = require("./stepperClasses");
var _StepConnector = _interopRequireDefault(require("../StepConnector"));
var _StepperContext = _interopRequireDefault(require("./StepperContext"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    orientation,
    nonLinear,
    alternativeLabel,
    classes
  } = ownerState;
  const slots = {
    root: ['root', orientation, nonLinear && 'nonLinear', alternativeLabel && 'alternativeLabel']
  };
  return (0, _composeClasses.default)(slots, _stepperClasses.getStepperUtilityClass, classes);
};
const StepperRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiStepper',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[ownerState.orientation], ownerState.alternativeLabel && styles.alternativeLabel, ownerState.nonLinear && styles.nonLinear];
  }
})({
  display: 'flex',
  variants: [{
    props: {
      orientation: 'horizontal'
    },
    style: {
      flexDirection: 'row',
      alignItems: 'center'
    }
  }, {
    props: {
      orientation: 'vertical'
    },
    style: {
      flexDirection: 'column'
    }
  }, {
    props: {
      alternativeLabel: true
    },
    style: {
      alignItems: 'flex-start'
    }
  }]
});
const defaultConnector = /*#__PURE__*/(0, _jsxRuntime.jsx)(_StepConnector.default, {});
const Stepper = /*#__PURE__*/React.forwardRef(function Stepper(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiStepper'
  });
  const {
    activeStep = 0,
    alternativeLabel = false,
    children,
    className,
    component = 'div',
    connector = defaultConnector,
    nonLinear = false,
    orientation = 'horizontal',
    ...other
  } = props;
  const ownerState = {
    ...props,
    nonLinear,
    alternativeLabel,
    orientation,
    component
  };
  const classes = useUtilityClasses(ownerState);
  const childrenArray = React.Children.toArray(children).filter(Boolean);
  const steps = childrenArray.map((step, index) => {
    return /*#__PURE__*/React.cloneElement(step, {
      index,
      last: index + 1 === childrenArray.length,
      ...step.props
    });
  });
  const contextValue = React.useMemo(() => ({
    activeStep,
    alternativeLabel,
    connector,
    nonLinear,
    orientation
  }), [activeStep, alternativeLabel, connector, nonLinear, orientation]);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_StepperContext.default.Provider, {
    value: contextValue,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(StepperRoot, {
      as: component,
      ownerState: ownerState,
      className: (0, _clsx.default)(classes.root, className),
      ref: ref,
      ...other,
      children: steps
    })
  });
});
process.env.NODE_ENV !== "production" ? Stepper.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Set the active step (zero based index).
   * Set to -1 to disable all the steps.
   * @default 0
   */
  activeStep: _integerPropType.default,
  /**
   * If set to 'true' and orientation is horizontal,
   * then the step label will be positioned under the icon.
   * @default false
   */
  alternativeLabel: _propTypes.default.bool,
  /**
   * Two or more `<Step />` components.
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
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * An element to be placed between each step.
   * @default <StepConnector />
   */
  connector: _propTypes.default.element,
  /**
   * If set the `Stepper` will not assist in controlling steps for linear flow.
   * @default false
   */
  nonLinear: _propTypes.default.bool,
  /**
   * The component orientation (layout flow direction).
   * @default 'horizontal'
   */
  orientation: _propTypes.default.oneOf(['horizontal', 'vertical']),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = Stepper;