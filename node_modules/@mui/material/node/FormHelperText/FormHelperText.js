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
var _formControlState = _interopRequireDefault(require("../FormControl/formControlState"));
var _useFormControl = _interopRequireDefault(require("../FormControl/useFormControl"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _formHelperTextClasses = _interopRequireWildcard(require("./formHelperTextClasses"));
var _jsxRuntime = require("react/jsx-runtime");
var _span;
const useUtilityClasses = ownerState => {
  const {
    classes,
    contained,
    size,
    disabled,
    error,
    filled,
    focused,
    required
  } = ownerState;
  const slots = {
    root: ['root', disabled && 'disabled', error && 'error', size && `size${(0, _capitalize.default)(size)}`, contained && 'contained', focused && 'focused', filled && 'filled', required && 'required']
  };
  return (0, _composeClasses.default)(slots, _formHelperTextClasses.getFormHelperTextUtilityClasses, classes);
};
const FormHelperTextRoot = (0, _zeroStyled.styled)('p', {
  name: 'MuiFormHelperText',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.size && styles[`size${(0, _capitalize.default)(ownerState.size)}`], ownerState.contained && styles.contained, ownerState.filled && styles.filled];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  color: (theme.vars || theme).palette.text.secondary,
  ...theme.typography.caption,
  textAlign: 'left',
  marginTop: 3,
  marginRight: 0,
  marginBottom: 0,
  marginLeft: 0,
  [`&.${_formHelperTextClasses.default.disabled}`]: {
    color: (theme.vars || theme).palette.text.disabled
  },
  [`&.${_formHelperTextClasses.default.error}`]: {
    color: (theme.vars || theme).palette.error.main
  },
  variants: [{
    props: {
      size: 'small'
    },
    style: {
      marginTop: 4
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.contained,
    style: {
      marginLeft: 14,
      marginRight: 14
    }
  }]
})));
const FormHelperText = /*#__PURE__*/React.forwardRef(function FormHelperText(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiFormHelperText'
  });
  const {
    children,
    className,
    component = 'p',
    disabled,
    error,
    filled,
    focused,
    margin,
    required,
    variant,
    ...other
  } = props;
  const muiFormControl = (0, _useFormControl.default)();
  const fcs = (0, _formControlState.default)({
    props,
    muiFormControl,
    states: ['variant', 'size', 'disabled', 'error', 'filled', 'focused', 'required']
  });
  const ownerState = {
    ...props,
    component,
    contained: fcs.variant === 'filled' || fcs.variant === 'outlined',
    variant: fcs.variant,
    size: fcs.size,
    disabled: fcs.disabled,
    error: fcs.error,
    filled: fcs.filled,
    focused: fcs.focused,
    required: fcs.required
  };

  // This issue explains why this is required: https://github.com/mui/material-ui/issues/42184
  delete ownerState.ownerState;
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(FormHelperTextRoot, {
    as: component,
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ...other,
    ownerState: ownerState,
    children: children === ' ' ? // notranslate needed while Google Translate will not fix zero-width space issue
    _span || (_span = /*#__PURE__*/(0, _jsxRuntime.jsx)("span", {
      className: "notranslate",
      children: "\u200B"
    })) : children
  });
});
process.env.NODE_ENV !== "production" ? FormHelperText.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
   *
   * If `' '` is provided, the component reserves one line height for displaying a future message.
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
   * If `true`, the helper text should be displayed in a disabled state.
   */
  disabled: _propTypes.default.bool,
  /**
   * If `true`, helper text should be displayed in an error state.
   */
  error: _propTypes.default.bool,
  /**
   * If `true`, the helper text should use filled classes key.
   */
  filled: _propTypes.default.bool,
  /**
   * If `true`, the helper text should use focused classes key.
   */
  focused: _propTypes.default.bool,
  /**
   * If `dense`, will adjust vertical spacing. This is normally obtained via context from
   * FormControl.
   */
  margin: _propTypes.default.oneOf(['dense']),
  /**
   * If `true`, the helper text should use required classes key.
   */
  required: _propTypes.default.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The variant to use.
   */
  variant: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['filled', 'outlined', 'standard']), _propTypes.default.string])
} : void 0;
var _default = exports.default = FormHelperText;