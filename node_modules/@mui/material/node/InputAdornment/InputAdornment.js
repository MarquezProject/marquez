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
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _Typography = _interopRequireDefault(require("../Typography"));
var _FormControlContext = _interopRequireDefault(require("../FormControl/FormControlContext"));
var _useFormControl = _interopRequireDefault(require("../FormControl/useFormControl"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _inputAdornmentClasses = _interopRequireWildcard(require("./inputAdornmentClasses"));
var _jsxRuntime = require("react/jsx-runtime");
var _span;
const overridesResolver = (props, styles) => {
  const {
    ownerState
  } = props;
  return [styles.root, styles[`position${(0, _capitalize.default)(ownerState.position)}`], ownerState.disablePointerEvents === true && styles.disablePointerEvents, styles[ownerState.variant]];
};
const useUtilityClasses = ownerState => {
  const {
    classes,
    disablePointerEvents,
    hiddenLabel,
    position,
    size,
    variant
  } = ownerState;
  const slots = {
    root: ['root', disablePointerEvents && 'disablePointerEvents', position && `position${(0, _capitalize.default)(position)}`, variant, hiddenLabel && 'hiddenLabel', size && `size${(0, _capitalize.default)(size)}`]
  };
  return (0, _composeClasses.default)(slots, _inputAdornmentClasses.getInputAdornmentUtilityClass, classes);
};
const InputAdornmentRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiInputAdornment',
  slot: 'Root',
  overridesResolver
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'flex',
  maxHeight: '2em',
  alignItems: 'center',
  whiteSpace: 'nowrap',
  color: (theme.vars || theme).palette.action.active,
  variants: [{
    props: {
      variant: 'filled'
    },
    style: {
      [`&.${_inputAdornmentClasses.default.positionStart}&:not(.${_inputAdornmentClasses.default.hiddenLabel})`]: {
        marginTop: 16
      }
    }
  }, {
    props: {
      position: 'start'
    },
    style: {
      marginRight: 8
    }
  }, {
    props: {
      position: 'end'
    },
    style: {
      marginLeft: 8
    }
  }, {
    props: {
      disablePointerEvents: true
    },
    style: {
      pointerEvents: 'none'
    }
  }]
})));
const InputAdornment = /*#__PURE__*/React.forwardRef(function InputAdornment(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiInputAdornment'
  });
  const {
    children,
    className,
    component = 'div',
    disablePointerEvents = false,
    disableTypography = false,
    position,
    variant: variantProp,
    ...other
  } = props;
  const muiFormControl = (0, _useFormControl.default)() || {};
  let variant = variantProp;
  if (variantProp && muiFormControl.variant) {
    if (process.env.NODE_ENV !== 'production') {
      if (variantProp === muiFormControl.variant) {
        console.error('MUI: The `InputAdornment` variant infers the variant prop ' + 'you do not have to provide one.');
      }
    }
  }
  if (muiFormControl && !variant) {
    variant = muiFormControl.variant;
  }
  const ownerState = {
    ...props,
    hiddenLabel: muiFormControl.hiddenLabel,
    size: muiFormControl.size,
    disablePointerEvents,
    position,
    variant
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_FormControlContext.default.Provider, {
    value: null,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(InputAdornmentRoot, {
      as: component,
      ownerState: ownerState,
      className: (0, _clsx.default)(classes.root, className),
      ref: ref,
      ...other,
      children: typeof children === 'string' && !disableTypography ? /*#__PURE__*/(0, _jsxRuntime.jsx)(_Typography.default, {
        color: "textSecondary",
        children: children
      }) : /*#__PURE__*/(0, _jsxRuntime.jsxs)(React.Fragment, {
        children: [position === 'start' ? (/* notranslate needed while Google Translate will not fix zero-width space issue */_span || (_span = /*#__PURE__*/(0, _jsxRuntime.jsx)("span", {
          className: "notranslate",
          children: "\u200B"
        }))) : null, children]
      })
    })
  });
});
process.env.NODE_ENV !== "production" ? InputAdornment.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component, normally an `IconButton` or string.
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
   * Disable pointer events on the root.
   * This allows for the content of the adornment to focus the `input` on click.
   * @default false
   */
  disablePointerEvents: _propTypes.default.bool,
  /**
   * If children is a string then disable wrapping in a Typography component.
   * @default false
   */
  disableTypography: _propTypes.default.bool,
  /**
   * The position this adornment should appear relative to the `Input`.
   */
  position: _propTypes.default.oneOf(['end', 'start']).isRequired,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The variant to use.
   * Note: If you are using the `TextField` component or the `FormControl` component
   * you do not have to set this manually.
   */
  variant: _propTypes.default.oneOf(['filled', 'outlined', 'standard'])
} : void 0;
var _default = exports.default = InputAdornment;