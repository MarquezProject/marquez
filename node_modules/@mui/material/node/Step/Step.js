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
var _StepperContext = _interopRequireDefault(require("../Stepper/StepperContext"));
var _StepContext = _interopRequireDefault(require("./StepContext"));
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _stepClasses = require("./stepClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    orientation,
    alternativeLabel,
    completed
  } = ownerState;
  const slots = {
    root: ['root', orientation, alternativeLabel && 'alternativeLabel', completed && 'completed']
  };
  return (0, _composeClasses.default)(slots, _stepClasses.getStepUtilityClass, classes);
};
const StepRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiStep',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[ownerState.orientation], ownerState.alternativeLabel && styles.alternativeLabel, ownerState.completed && styles.completed];
  }
})({
  variants: [{
    props: {
      orientation: 'horizontal'
    },
    style: {
      paddingLeft: 8,
      paddingRight: 8
    }
  }, {
    props: {
      alternativeLabel: true
    },
    style: {
      flex: 1,
      position: 'relative'
    }
  }]
});
const Step = /*#__PURE__*/React.forwardRef(function Step(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiStep'
  });
  const {
    active: activeProp,
    children,
    className,
    component = 'div',
    completed: completedProp,
    disabled: disabledProp,
    expanded = false,
    index,
    last,
    ...other
  } = props;
  const {
    activeStep,
    connector,
    alternativeLabel,
    orientation,
    nonLinear
  } = React.useContext(_StepperContext.default);
  let [active = false, completed = false, disabled = false] = [activeProp, completedProp, disabledProp];
  if (activeStep === index) {
    active = activeProp !== undefined ? activeProp : true;
  } else if (!nonLinear && activeStep > index) {
    completed = completedProp !== undefined ? completedProp : true;
  } else if (!nonLinear && activeStep < index) {
    disabled = disabledProp !== undefined ? disabledProp : true;
  }
  const contextValue = React.useMemo(() => ({
    index,
    last,
    expanded,
    icon: index + 1,
    active,
    completed,
    disabled
  }), [index, last, expanded, active, completed, disabled]);
  const ownerState = {
    ...props,
    active,
    orientation,
    alternativeLabel,
    completed,
    disabled,
    expanded,
    component
  };
  const classes = useUtilityClasses(ownerState);
  const newChildren = /*#__PURE__*/(0, _jsxRuntime.jsxs)(StepRoot, {
    as: component,
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other,
    children: [connector && alternativeLabel && index !== 0 ? connector : null, children]
  });
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_StepContext.default.Provider, {
    value: contextValue,
    children: connector && !alternativeLabel && index !== 0 ? /*#__PURE__*/(0, _jsxRuntime.jsxs)(React.Fragment, {
      children: [connector, newChildren]
    }) : newChildren
  });
});
process.env.NODE_ENV !== "production" ? Step.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Sets the step as active. Is passed to child components.
   */
  active: _propTypes.default.bool,
  /**
   * Should be `Step` sub-components such as `StepLabel`, `StepContent`.
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
   * Mark the step as completed. Is passed to child components.
   */
  completed: _propTypes.default.bool,
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * If `true`, the step is disabled, will also disable the button if
   * `StepButton` is a child of `Step`. Is passed to child components.
   */
  disabled: _propTypes.default.bool,
  /**
   * Expand the step.
   * @default false
   */
  expanded: _propTypes.default.bool,
  /**
   * The position of the step.
   * The prop defaults to the value inherited from the parent Stepper component.
   */
  index: _integerPropType.default,
  /**
   * If `true`, the Step is displayed as rendered last.
   * The prop defaults to the value inherited from the parent Stepper component.
   */
  last: _propTypes.default.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = Step;