"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _clsx = _interopRequireDefault(require("clsx"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var React = _interopRequireWildcard(require("react"));
var _StepContext = _interopRequireDefault(require("../Step/StepContext"));
var _StepIcon = _interopRequireDefault(require("../StepIcon"));
var _StepperContext = _interopRequireDefault(require("../Stepper/StepperContext"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _stepLabelClasses = _interopRequireWildcard(require("./stepLabelClasses"));
var _useSlot = _interopRequireDefault(require("../utils/useSlot"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    orientation,
    active,
    completed,
    error,
    disabled,
    alternativeLabel
  } = ownerState;
  const slots = {
    root: ['root', orientation, error && 'error', disabled && 'disabled', alternativeLabel && 'alternativeLabel'],
    label: ['label', active && 'active', completed && 'completed', error && 'error', disabled && 'disabled', alternativeLabel && 'alternativeLabel'],
    iconContainer: ['iconContainer', active && 'active', completed && 'completed', error && 'error', disabled && 'disabled', alternativeLabel && 'alternativeLabel'],
    labelContainer: ['labelContainer', alternativeLabel && 'alternativeLabel']
  };
  return (0, _composeClasses.default)(slots, _stepLabelClasses.getStepLabelUtilityClass, classes);
};
const StepLabelRoot = (0, _zeroStyled.styled)('span', {
  name: 'MuiStepLabel',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[ownerState.orientation]];
  }
})({
  display: 'flex',
  alignItems: 'center',
  [`&.${_stepLabelClasses.default.alternativeLabel}`]: {
    flexDirection: 'column'
  },
  [`&.${_stepLabelClasses.default.disabled}`]: {
    cursor: 'default'
  },
  variants: [{
    props: {
      orientation: 'vertical'
    },
    style: {
      textAlign: 'left',
      padding: '8px 0'
    }
  }]
});
const StepLabelLabel = (0, _zeroStyled.styled)('span', {
  name: 'MuiStepLabel',
  slot: 'Label',
  overridesResolver: (props, styles) => styles.label
})((0, _memoTheme.default)(({
  theme
}) => ({
  ...theme.typography.body2,
  display: 'block',
  transition: theme.transitions.create('color', {
    duration: theme.transitions.duration.shortest
  }),
  [`&.${_stepLabelClasses.default.active}`]: {
    color: (theme.vars || theme).palette.text.primary,
    fontWeight: 500
  },
  [`&.${_stepLabelClasses.default.completed}`]: {
    color: (theme.vars || theme).palette.text.primary,
    fontWeight: 500
  },
  [`&.${_stepLabelClasses.default.alternativeLabel}`]: {
    marginTop: 16
  },
  [`&.${_stepLabelClasses.default.error}`]: {
    color: (theme.vars || theme).palette.error.main
  }
})));
const StepLabelIconContainer = (0, _zeroStyled.styled)('span', {
  name: 'MuiStepLabel',
  slot: 'IconContainer',
  overridesResolver: (props, styles) => styles.iconContainer
})({
  flexShrink: 0,
  display: 'flex',
  paddingRight: 8,
  [`&.${_stepLabelClasses.default.alternativeLabel}`]: {
    paddingRight: 0
  }
});
const StepLabelLabelContainer = (0, _zeroStyled.styled)('span', {
  name: 'MuiStepLabel',
  slot: 'LabelContainer',
  overridesResolver: (props, styles) => styles.labelContainer
})((0, _memoTheme.default)(({
  theme
}) => ({
  width: '100%',
  color: (theme.vars || theme).palette.text.secondary,
  [`&.${_stepLabelClasses.default.alternativeLabel}`]: {
    textAlign: 'center'
  }
})));
const StepLabel = /*#__PURE__*/React.forwardRef(function StepLabel(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiStepLabel'
  });
  const {
    children,
    className,
    componentsProps = {},
    error = false,
    icon: iconProp,
    optional,
    slots = {},
    slotProps = {},
    StepIconComponent: StepIconComponentProp,
    StepIconProps,
    ...other
  } = props;
  const {
    alternativeLabel,
    orientation
  } = React.useContext(_StepperContext.default);
  const {
    active,
    disabled,
    completed,
    icon: iconContext
  } = React.useContext(_StepContext.default);
  const icon = iconProp || iconContext;
  let StepIconComponent = StepIconComponentProp;
  if (icon && !StepIconComponent) {
    StepIconComponent = _StepIcon.default;
  }
  const ownerState = {
    ...props,
    active,
    alternativeLabel,
    completed,
    disabled,
    error,
    orientation
  };
  const classes = useUtilityClasses(ownerState);
  const externalForwardedProps = {
    slots,
    slotProps: {
      stepIcon: StepIconProps,
      ...componentsProps,
      ...slotProps
    }
  };
  const [LabelSlot, labelProps] = (0, _useSlot.default)('label', {
    elementType: StepLabelLabel,
    externalForwardedProps,
    ownerState
  });
  const [StepIconSlot, stepIconProps] = (0, _useSlot.default)('stepIcon', {
    elementType: StepIconComponent,
    externalForwardedProps,
    ownerState
  });
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(StepLabelRoot, {
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other,
    children: [icon || StepIconSlot ? /*#__PURE__*/(0, _jsxRuntime.jsx)(StepLabelIconContainer, {
      className: classes.iconContainer,
      ownerState: ownerState,
      children: /*#__PURE__*/(0, _jsxRuntime.jsx)(StepIconSlot, {
        completed: completed,
        active: active,
        error: error,
        icon: icon,
        ...stepIconProps
      })
    }) : null, /*#__PURE__*/(0, _jsxRuntime.jsxs)(StepLabelLabelContainer, {
      className: classes.labelContainer,
      ownerState: ownerState,
      children: [children ? /*#__PURE__*/(0, _jsxRuntime.jsx)(LabelSlot, {
        ...labelProps,
        className: (0, _clsx.default)(classes.label, labelProps?.className),
        children: children
      }) : null, optional]
    })]
  });
});
process.env.NODE_ENV !== "production" ? StepLabel.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * In most cases will simply be a string containing a title for the label.
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
   * The props used for each slot inside.
   * @default {}
   * @deprecated use the `slotProps` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   */
  componentsProps: _propTypes.default.shape({
    label: _propTypes.default.object
  }),
  /**
   * If `true`, the step is marked as failed.
   * @default false
   */
  error: _propTypes.default.bool,
  /**
   * Override the default label of the step icon.
   */
  icon: _propTypes.default.node,
  /**
   * The optional node to display.
   */
  optional: _propTypes.default.node,
  /**
   * The props used for each slot inside.
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    label: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    stepIcon: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * The components used for each slot inside.
   * @default {}
   */
  slots: _propTypes.default.shape({
    label: _propTypes.default.elementType,
    stepIcon: _propTypes.default.elementType
  }),
  /**
   * The component to render in place of the [`StepIcon`](https://mui.com/material-ui/api/step-icon/).
   */
  StepIconComponent: _propTypes.default.elementType,
  /**
   * Props applied to the [`StepIcon`](https://mui.com/material-ui/api/step-icon/) element.
   */
  StepIconProps: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
if (StepLabel) {
  StepLabel.muiName = 'StepLabel';
}
var _default = exports.default = StepLabel;