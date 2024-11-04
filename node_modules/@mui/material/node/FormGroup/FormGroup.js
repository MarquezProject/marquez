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
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _formGroupClasses = require("./formGroupClasses");
var _useFormControl = _interopRequireDefault(require("../FormControl/useFormControl"));
var _formControlState = _interopRequireDefault(require("../FormControl/formControlState"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    row,
    error
  } = ownerState;
  const slots = {
    root: ['root', row && 'row', error && 'error']
  };
  return (0, _composeClasses.default)(slots, _formGroupClasses.getFormGroupUtilityClass, classes);
};
const FormGroupRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiFormGroup',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.row && styles.row];
  }
})({
  display: 'flex',
  flexDirection: 'column',
  flexWrap: 'wrap',
  variants: [{
    props: {
      row: true
    },
    style: {
      flexDirection: 'row'
    }
  }]
});

/**
 * `FormGroup` wraps controls such as `Checkbox` and `Switch`.
 * It provides compact row layout.
 * For the `Radio`, you should be using the `RadioGroup` component instead of this one.
 */
const FormGroup = /*#__PURE__*/React.forwardRef(function FormGroup(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiFormGroup'
  });
  const {
    className,
    row = false,
    ...other
  } = props;
  const muiFormControl = (0, _useFormControl.default)();
  const fcs = (0, _formControlState.default)({
    props,
    muiFormControl,
    states: ['error']
  });
  const ownerState = {
    ...props,
    row,
    error: fcs.error
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(FormGroupRoot, {
    className: (0, _clsx.default)(classes.root, className),
    ownerState: ownerState,
    ref: ref,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? FormGroup.propTypes /* remove-proptypes */ = {
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
   * Display group of elements in a compact row.
   * @default false
   */
  row: _propTypes.default.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = FormGroup;