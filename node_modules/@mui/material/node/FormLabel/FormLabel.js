"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = exports.FormLabelRoot = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _formControlState = _interopRequireDefault(require("../FormControl/formControlState"));
var _useFormControl = _interopRequireDefault(require("../FormControl/useFormControl"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _createSimplePaletteValueFilter = _interopRequireDefault(require("../utils/createSimplePaletteValueFilter"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _formLabelClasses = _interopRequireWildcard(require("./formLabelClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    color,
    focused,
    disabled,
    error,
    filled,
    required
  } = ownerState;
  const slots = {
    root: ['root', `color${(0, _capitalize.default)(color)}`, disabled && 'disabled', error && 'error', filled && 'filled', focused && 'focused', required && 'required'],
    asterisk: ['asterisk', error && 'error']
  };
  return (0, _composeClasses.default)(slots, _formLabelClasses.getFormLabelUtilityClasses, classes);
};
const FormLabelRoot = exports.FormLabelRoot = (0, _zeroStyled.styled)('label', {
  name: 'MuiFormLabel',
  slot: 'Root',
  overridesResolver: ({
    ownerState
  }, styles) => {
    return {
      ...styles.root,
      ...(ownerState.color === 'secondary' && styles.colorSecondary),
      ...(ownerState.filled && styles.filled)
    };
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  color: (theme.vars || theme).palette.text.secondary,
  ...theme.typography.body1,
  lineHeight: '1.4375em',
  padding: 0,
  position: 'relative',
  variants: [...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)()).map(([color]) => ({
    props: {
      color
    },
    style: {
      [`&.${_formLabelClasses.default.focused}`]: {
        color: (theme.vars || theme).palette[color].main
      }
    }
  })), {
    props: {},
    style: {
      [`&.${_formLabelClasses.default.disabled}`]: {
        color: (theme.vars || theme).palette.text.disabled
      },
      [`&.${_formLabelClasses.default.error}`]: {
        color: (theme.vars || theme).palette.error.main
      }
    }
  }]
})));
const AsteriskComponent = (0, _zeroStyled.styled)('span', {
  name: 'MuiFormLabel',
  slot: 'Asterisk',
  overridesResolver: (props, styles) => styles.asterisk
})((0, _memoTheme.default)(({
  theme
}) => ({
  [`&.${_formLabelClasses.default.error}`]: {
    color: (theme.vars || theme).palette.error.main
  }
})));
const FormLabel = /*#__PURE__*/React.forwardRef(function FormLabel(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiFormLabel'
  });
  const {
    children,
    className,
    color,
    component = 'label',
    disabled,
    error,
    filled,
    focused,
    required,
    ...other
  } = props;
  const muiFormControl = (0, _useFormControl.default)();
  const fcs = (0, _formControlState.default)({
    props,
    muiFormControl,
    states: ['color', 'required', 'focused', 'disabled', 'error', 'filled']
  });
  const ownerState = {
    ...props,
    color: fcs.color || 'primary',
    component,
    disabled: fcs.disabled,
    error: fcs.error,
    filled: fcs.filled,
    focused: fcs.focused,
    required: fcs.required
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(FormLabelRoot, {
    as: component,
    ownerState: ownerState,
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ...other,
    children: [children, fcs.required && /*#__PURE__*/(0, _jsxRuntime.jsxs)(AsteriskComponent, {
      ownerState: ownerState,
      "aria-hidden": true,
      className: classes.asterisk,
      children: ["\u2009", '*']
    })]
  });
});
process.env.NODE_ENV !== "production" ? FormLabel.propTypes /* remove-proptypes */ = {
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
   * The color of the component.
   * It supports both default and custom theme colors, which can be added as shown in the
   * [palette customization guide](https://mui.com/material-ui/customization/palette/#custom-colors).
   */
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['error', 'info', 'primary', 'secondary', 'success', 'warning']), _propTypes.default.string]),
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * If `true`, the label should be displayed in a disabled state.
   */
  disabled: _propTypes.default.bool,
  /**
   * If `true`, the label is displayed in an error state.
   */
  error: _propTypes.default.bool,
  /**
   * If `true`, the label should use filled classes key.
   */
  filled: _propTypes.default.bool,
  /**
   * If `true`, the input of this label is focused (used by `FormGroup` components).
   */
  focused: _propTypes.default.bool,
  /**
   * If `true`, the label will indicate that the `input` is required.
   */
  required: _propTypes.default.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = FormLabel;