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
var _Paper = _interopRequireDefault(require("../Paper"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _LinearProgress = _interopRequireDefault(require("../LinearProgress"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _slotShouldForwardProp = _interopRequireDefault(require("../styles/slotShouldForwardProp"));
var _mobileStepperClasses = require("./mobileStepperClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    position
  } = ownerState;
  const slots = {
    root: ['root', `position${(0, _capitalize.default)(position)}`],
    dots: ['dots'],
    dot: ['dot'],
    dotActive: ['dotActive'],
    progress: ['progress']
  };
  return (0, _composeClasses.default)(slots, _mobileStepperClasses.getMobileStepperUtilityClass, classes);
};
const MobileStepperRoot = (0, _zeroStyled.styled)(_Paper.default, {
  name: 'MuiMobileStepper',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[`position${(0, _capitalize.default)(ownerState.position)}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'flex',
  flexDirection: 'row',
  justifyContent: 'space-between',
  alignItems: 'center',
  background: (theme.vars || theme).palette.background.default,
  padding: 8,
  variants: [{
    props: ({
      position
    }) => position === 'top' || position === 'bottom',
    style: {
      position: 'fixed',
      left: 0,
      right: 0,
      zIndex: (theme.vars || theme).zIndex.mobileStepper
    }
  }, {
    props: {
      position: 'top'
    },
    style: {
      top: 0
    }
  }, {
    props: {
      position: 'bottom'
    },
    style: {
      bottom: 0
    }
  }]
})));
const MobileStepperDots = (0, _zeroStyled.styled)('div', {
  name: 'MuiMobileStepper',
  slot: 'Dots',
  overridesResolver: (props, styles) => styles.dots
})({
  variants: [{
    props: {
      variant: 'dots'
    },
    style: {
      display: 'flex',
      flexDirection: 'row'
    }
  }]
});
const MobileStepperDot = (0, _zeroStyled.styled)('div', {
  name: 'MuiMobileStepper',
  slot: 'Dot',
  shouldForwardProp: prop => (0, _slotShouldForwardProp.default)(prop) && prop !== 'dotActive',
  overridesResolver: (props, styles) => {
    const {
      dotActive
    } = props;
    return [styles.dot, dotActive && styles.dotActive];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  variants: [{
    props: {
      variant: 'dots'
    },
    style: {
      transition: theme.transitions.create('background-color', {
        duration: theme.transitions.duration.shortest
      }),
      backgroundColor: (theme.vars || theme).palette.action.disabled,
      borderRadius: '50%',
      width: 8,
      height: 8,
      margin: '0 2px'
    }
  }, {
    props: {
      variant: 'dots',
      dotActive: true
    },
    style: {
      backgroundColor: (theme.vars || theme).palette.primary.main
    }
  }]
})));
const MobileStepperProgress = (0, _zeroStyled.styled)(_LinearProgress.default, {
  name: 'MuiMobileStepper',
  slot: 'Progress',
  overridesResolver: (props, styles) => styles.progress
})({
  variants: [{
    props: {
      variant: 'progress'
    },
    style: {
      width: '50%'
    }
  }]
});
const MobileStepper = /*#__PURE__*/React.forwardRef(function MobileStepper(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiMobileStepper'
  });
  const {
    activeStep = 0,
    backButton,
    className,
    LinearProgressProps,
    nextButton,
    position = 'bottom',
    steps,
    variant = 'dots',
    ...other
  } = props;
  const ownerState = {
    ...props,
    activeStep,
    position,
    variant
  };
  let value;
  if (variant === 'progress') {
    if (steps === 1) {
      value = 100;
    } else {
      value = Math.ceil(activeStep / (steps - 1) * 100);
    }
  }
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(MobileStepperRoot, {
    square: true,
    elevation: 0,
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other,
    children: [backButton, variant === 'text' && /*#__PURE__*/(0, _jsxRuntime.jsxs)(React.Fragment, {
      children: [activeStep + 1, " / ", steps]
    }), variant === 'dots' && /*#__PURE__*/(0, _jsxRuntime.jsx)(MobileStepperDots, {
      ownerState: ownerState,
      className: classes.dots,
      children: [...new Array(steps)].map((_, index) => /*#__PURE__*/(0, _jsxRuntime.jsx)(MobileStepperDot, {
        className: (0, _clsx.default)(classes.dot, index === activeStep && classes.dotActive),
        ownerState: ownerState,
        dotActive: index === activeStep
      }, index))
    }), variant === 'progress' && /*#__PURE__*/(0, _jsxRuntime.jsx)(MobileStepperProgress, {
      ownerState: ownerState,
      className: classes.progress,
      variant: "determinate",
      value: value,
      ...LinearProgressProps
    }), nextButton]
  });
});
process.env.NODE_ENV !== "production" ? MobileStepper.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Set the active step (zero based index).
   * Defines which dot is highlighted when the variant is 'dots'.
   * @default 0
   */
  activeStep: _integerPropType.default,
  /**
   * A back button element. For instance, it can be a `Button` or an `IconButton`.
   */
  backButton: _propTypes.default.node,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * Props applied to the `LinearProgress` element.
   */
  LinearProgressProps: _propTypes.default.object,
  /**
   * A next button element. For instance, it can be a `Button` or an `IconButton`.
   */
  nextButton: _propTypes.default.node,
  /**
   * Set the positioning type.
   * @default 'bottom'
   */
  position: _propTypes.default.oneOf(['bottom', 'static', 'top']),
  /**
   * The total steps.
   */
  steps: _integerPropType.default.isRequired,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The variant to use.
   * @default 'dots'
   */
  variant: _propTypes.default.oneOf(['dots', 'progress', 'text'])
} : void 0;
var _default = exports.default = MobileStepper;