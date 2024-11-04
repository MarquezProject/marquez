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
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _StepperContext = _interopRequireDefault(require("../Stepper/StepperContext"));
var _StepContext = _interopRequireDefault(require("../Step/StepContext"));
var _stepConnectorClasses = require("./stepConnectorClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    orientation,
    alternativeLabel,
    active,
    completed,
    disabled
  } = ownerState;
  const slots = {
    root: ['root', orientation, alternativeLabel && 'alternativeLabel', active && 'active', completed && 'completed', disabled && 'disabled'],
    line: ['line', `line${(0, _capitalize.default)(orientation)}`]
  };
  return (0, _composeClasses.default)(slots, _stepConnectorClasses.getStepConnectorUtilityClass, classes);
};
const StepConnectorRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiStepConnector',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[ownerState.orientation], ownerState.alternativeLabel && styles.alternativeLabel, ownerState.completed && styles.completed];
  }
})({
  flex: '1 1 auto',
  variants: [{
    props: {
      orientation: 'vertical'
    },
    style: {
      marginLeft: 12 // half icon
    }
  }, {
    props: {
      alternativeLabel: true
    },
    style: {
      position: 'absolute',
      top: 8 + 4,
      left: 'calc(-50% + 20px)',
      right: 'calc(50% + 20px)'
    }
  }]
});
const StepConnectorLine = (0, _zeroStyled.styled)('span', {
  name: 'MuiStepConnector',
  slot: 'Line',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.line, styles[`line${(0, _capitalize.default)(ownerState.orientation)}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => {
  const borderColor = theme.palette.mode === 'light' ? theme.palette.grey[400] : theme.palette.grey[600];
  return {
    display: 'block',
    borderColor: theme.vars ? theme.vars.palette.StepConnector.border : borderColor,
    variants: [{
      props: {
        orientation: 'horizontal'
      },
      style: {
        borderTopStyle: 'solid',
        borderTopWidth: 1
      }
    }, {
      props: {
        orientation: 'vertical'
      },
      style: {
        borderLeftStyle: 'solid',
        borderLeftWidth: 1,
        minHeight: 24
      }
    }]
  };
}));
const StepConnector = /*#__PURE__*/React.forwardRef(function StepConnector(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiStepConnector'
  });
  const {
    className,
    ...other
  } = props;
  const {
    alternativeLabel,
    orientation = 'horizontal'
  } = React.useContext(_StepperContext.default);
  const {
    active,
    disabled,
    completed
  } = React.useContext(_StepContext.default);
  const ownerState = {
    ...props,
    alternativeLabel,
    orientation,
    active,
    completed,
    disabled
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(StepConnectorRoot, {
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(StepConnectorLine, {
      className: classes.line,
      ownerState: ownerState
    })
  });
});
process.env.NODE_ENV !== "production" ? StepConnector.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
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
var _default = exports.default = StepConnector;