"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = exports.FormControlLabelRoot = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _refType = _interopRequireDefault(require("@mui/utils/refType"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _FormControl = require("../FormControl");
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _Typography = _interopRequireDefault(require("../Typography"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _formControlLabelClasses = _interopRequireWildcard(require("./formControlLabelClasses"));
var _formControlState = _interopRequireDefault(require("../FormControl/formControlState"));
var _useSlot = _interopRequireDefault(require("../utils/useSlot"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    disabled,
    labelPlacement,
    error,
    required
  } = ownerState;
  const slots = {
    root: ['root', disabled && 'disabled', `labelPlacement${(0, _capitalize.default)(labelPlacement)}`, error && 'error', required && 'required'],
    label: ['label', disabled && 'disabled'],
    asterisk: ['asterisk', error && 'error']
  };
  return (0, _composeClasses.default)(slots, _formControlLabelClasses.getFormControlLabelUtilityClasses, classes);
};
const FormControlLabelRoot = exports.FormControlLabelRoot = (0, _zeroStyled.styled)('label', {
  name: 'MuiFormControlLabel',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [{
      [`& .${_formControlLabelClasses.default.label}`]: styles.label
    }, styles.root, styles[`labelPlacement${(0, _capitalize.default)(ownerState.labelPlacement)}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'inline-flex',
  alignItems: 'center',
  cursor: 'pointer',
  // For correct alignment with the text.
  verticalAlign: 'middle',
  WebkitTapHighlightColor: 'transparent',
  marginLeft: -11,
  marginRight: 16,
  // used for row presentation of radio/checkbox
  [`&.${_formControlLabelClasses.default.disabled}`]: {
    cursor: 'default'
  },
  [`& .${_formControlLabelClasses.default.label}`]: {
    [`&.${_formControlLabelClasses.default.disabled}`]: {
      color: (theme.vars || theme).palette.text.disabled
    }
  },
  variants: [{
    props: {
      labelPlacement: 'start'
    },
    style: {
      flexDirection: 'row-reverse',
      marginRight: -11
    }
  }, {
    props: {
      labelPlacement: 'top'
    },
    style: {
      flexDirection: 'column-reverse'
    }
  }, {
    props: {
      labelPlacement: 'bottom'
    },
    style: {
      flexDirection: 'column'
    }
  }, {
    props: ({
      labelPlacement
    }) => labelPlacement === 'start' || labelPlacement === 'top' || labelPlacement === 'bottom',
    style: {
      marginLeft: 16 // used for row presentation of radio/checkbox
    }
  }]
})));
const AsteriskComponent = (0, _zeroStyled.styled)('span', {
  name: 'MuiFormControlLabel',
  slot: 'Asterisk',
  overridesResolver: (props, styles) => styles.asterisk
})((0, _memoTheme.default)(({
  theme
}) => ({
  [`&.${_formControlLabelClasses.default.error}`]: {
    color: (theme.vars || theme).palette.error.main
  }
})));

/**
 * Drop-in replacement of the `Radio`, `Switch` and `Checkbox` component.
 * Use this component if you want to display an extra label.
 */
const FormControlLabel = /*#__PURE__*/React.forwardRef(function FormControlLabel(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiFormControlLabel'
  });
  const {
    checked,
    className,
    componentsProps = {},
    control,
    disabled: disabledProp,
    disableTypography,
    inputRef,
    label: labelProp,
    labelPlacement = 'end',
    name,
    onChange,
    required: requiredProp,
    slots = {},
    slotProps = {},
    value,
    ...other
  } = props;
  const muiFormControl = (0, _FormControl.useFormControl)();
  const disabled = disabledProp ?? control.props.disabled ?? muiFormControl?.disabled;
  const required = requiredProp ?? control.props.required;
  const controlProps = {
    disabled,
    required
  };
  ['checked', 'name', 'onChange', 'value', 'inputRef'].forEach(key => {
    if (typeof control.props[key] === 'undefined' && typeof props[key] !== 'undefined') {
      controlProps[key] = props[key];
    }
  });
  const fcs = (0, _formControlState.default)({
    props,
    muiFormControl,
    states: ['error']
  });
  const ownerState = {
    ...props,
    disabled,
    labelPlacement,
    required,
    error: fcs.error
  };
  const classes = useUtilityClasses(ownerState);
  const externalForwardedProps = {
    slots,
    slotProps: {
      ...componentsProps,
      ...slotProps
    }
  };
  const [TypographySlot, typographySlotProps] = (0, _useSlot.default)('typography', {
    elementType: _Typography.default,
    externalForwardedProps,
    ownerState
  });
  let label = labelProp;
  if (label != null && label.type !== _Typography.default && !disableTypography) {
    label = /*#__PURE__*/(0, _jsxRuntime.jsx)(TypographySlot, {
      component: "span",
      ...typographySlotProps,
      className: (0, _clsx.default)(classes.label, typographySlotProps?.className),
      children: label
    });
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(FormControlLabelRoot, {
    className: (0, _clsx.default)(classes.root, className),
    ownerState: ownerState,
    ref: ref,
    ...other,
    children: [/*#__PURE__*/React.cloneElement(control, controlProps), required ? /*#__PURE__*/(0, _jsxRuntime.jsxs)("div", {
      children: [label, /*#__PURE__*/(0, _jsxRuntime.jsxs)(AsteriskComponent, {
        ownerState: ownerState,
        "aria-hidden": true,
        className: classes.asterisk,
        children: ["\u2009", '*']
      })]
    }) : label]
  });
});
process.env.NODE_ENV !== "production" ? FormControlLabel.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * If `true`, the component appears selected.
   */
  checked: _propTypes.default.bool,
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
    typography: _propTypes.default.object
  }),
  /**
   * A control element. For instance, it can be a `Radio`, a `Switch` or a `Checkbox`.
   */
  control: _propTypes.default.element.isRequired,
  /**
   * If `true`, the control is disabled.
   */
  disabled: _propTypes.default.bool,
  /**
   * If `true`, the label is rendered as it is passed without an additional typography node.
   */
  disableTypography: _propTypes.default.bool,
  /**
   * Pass a ref to the `input` element.
   */
  inputRef: _refType.default,
  /**
   * A text or an element to be used in an enclosing label element.
   */
  label: _propTypes.default.node,
  /**
   * The position of the label.
   * @default 'end'
   */
  labelPlacement: _propTypes.default.oneOf(['bottom', 'end', 'start', 'top']),
  /**
   * @ignore
   */
  name: _propTypes.default.string,
  /**
   * Callback fired when the state is changed.
   *
   * @param {React.SyntheticEvent} event The event source of the callback.
   * You can pull out the new checked state by accessing `event.target.checked` (boolean).
   */
  onChange: _propTypes.default.func,
  /**
   * If `true`, the label will indicate that the `input` is required.
   */
  required: _propTypes.default.bool,
  /**
   * The props used for each slot inside.
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    typography: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * The components used for each slot inside.
   * @default {}
   */
  slots: _propTypes.default.shape({
    typography: _propTypes.default.elementType
  }),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The value of the component.
   */
  value: _propTypes.default.any
} : void 0;
var _default = exports.default = FormControlLabel;