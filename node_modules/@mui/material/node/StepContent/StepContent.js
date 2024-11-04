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
var _Collapse = _interopRequireDefault(require("../Collapse"));
var _StepperContext = _interopRequireDefault(require("../Stepper/StepperContext"));
var _StepContext = _interopRequireDefault(require("../Step/StepContext"));
var _stepContentClasses = require("./stepContentClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    last
  } = ownerState;
  const slots = {
    root: ['root', last && 'last'],
    transition: ['transition']
  };
  return (0, _composeClasses.default)(slots, _stepContentClasses.getStepContentUtilityClass, classes);
};
const StepContentRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiStepContent',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.last && styles.last];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  marginLeft: 12,
  // half icon
  paddingLeft: 8 + 12,
  // margin + half icon
  paddingRight: 8,
  borderLeft: theme.vars ? `1px solid ${theme.vars.palette.StepContent.border}` : `1px solid ${theme.palette.mode === 'light' ? theme.palette.grey[400] : theme.palette.grey[600]}`,
  variants: [{
    props: {
      last: true
    },
    style: {
      borderLeft: 'none'
    }
  }]
})));
const StepContentTransition = (0, _zeroStyled.styled)(_Collapse.default, {
  name: 'MuiStepContent',
  slot: 'Transition',
  overridesResolver: (props, styles) => styles.transition
})({});
const StepContent = /*#__PURE__*/React.forwardRef(function StepContent(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiStepContent'
  });
  const {
    children,
    className,
    TransitionComponent = _Collapse.default,
    transitionDuration: transitionDurationProp = 'auto',
    TransitionProps,
    ...other
  } = props;
  const {
    orientation
  } = React.useContext(_StepperContext.default);
  const {
    active,
    last,
    expanded
  } = React.useContext(_StepContext.default);
  const ownerState = {
    ...props,
    last
  };
  const classes = useUtilityClasses(ownerState);
  if (process.env.NODE_ENV !== 'production') {
    if (orientation !== 'vertical') {
      console.error('MUI: <StepContent /> is only designed for use with the vertical stepper.');
    }
  }
  let transitionDuration = transitionDurationProp;
  if (transitionDurationProp === 'auto' && !TransitionComponent.muiSupportAuto) {
    transitionDuration = undefined;
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(StepContentRoot, {
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(StepContentTransition, {
      as: TransitionComponent,
      in: active || expanded,
      className: classes.transition,
      ownerState: ownerState,
      timeout: transitionDuration,
      unmountOnExit: true,
      ...TransitionProps,
      children: children
    })
  });
});
process.env.NODE_ENV !== "production" ? StepContent.propTypes /* remove-proptypes */ = {
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
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The component used for the transition.
   * [Follow this guide](https://mui.com/material-ui/transitions/#transitioncomponent-prop) to learn more about the requirements for this component.
   * @default Collapse
   */
  TransitionComponent: _propTypes.default.elementType,
  /**
   * Adjust the duration of the content expand transition.
   * Passed as a prop to the transition component.
   *
   * Set to 'auto' to automatically calculate transition time based on height.
   * @default 'auto'
   */
  transitionDuration: _propTypes.default.oneOfType([_propTypes.default.oneOf(['auto']), _propTypes.default.number, _propTypes.default.shape({
    appear: _propTypes.default.number,
    enter: _propTypes.default.number,
    exit: _propTypes.default.number
  })]),
  /**
   * Props applied to the transition element.
   * By default, the element is based on this [`Transition`](https://reactcommunity.org/react-transition-group/transition/) component.
   */
  TransitionProps: _propTypes.default.object
} : void 0;
var _default = exports.default = StepContent;