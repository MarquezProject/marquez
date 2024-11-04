"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _clsx = _interopRequireDefault(require("clsx"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _NativeSelectInput = _interopRequireDefault(require("./NativeSelectInput"));
var _formControlState = _interopRequireDefault(require("../FormControl/formControlState"));
var _useFormControl = _interopRequireDefault(require("../FormControl/useFormControl"));
var _ArrowDropDown = _interopRequireDefault(require("../internal/svg-icons/ArrowDropDown"));
var _Input = _interopRequireDefault(require("../Input"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _nativeSelectClasses = require("./nativeSelectClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes
  } = ownerState;
  const slots = {
    root: ['root']
  };
  return (0, _composeClasses.default)(slots, _nativeSelectClasses.getNativeSelectUtilityClasses, classes);
};
const defaultInput = /*#__PURE__*/(0, _jsxRuntime.jsx)(_Input.default, {});
/**
 * An alternative to `<Select native />` with a much smaller bundle size footprint.
 */
const NativeSelect = /*#__PURE__*/React.forwardRef(function NativeSelect(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    name: 'MuiNativeSelect',
    props: inProps
  });
  const {
    className,
    children,
    classes: classesProp = {},
    IconComponent = _ArrowDropDown.default,
    input = defaultInput,
    inputProps,
    variant,
    ...other
  } = props;
  const muiFormControl = (0, _useFormControl.default)();
  const fcs = (0, _formControlState.default)({
    props,
    muiFormControl,
    states: ['variant']
  });
  const ownerState = {
    ...props,
    classes: classesProp
  };
  const classes = useUtilityClasses(ownerState);
  const {
    root,
    ...otherClasses
  } = classesProp;
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(React.Fragment, {
    children: /*#__PURE__*/React.cloneElement(input, {
      // Most of the logic is implemented in `NativeSelectInput`.
      // The `Select` component is a simple API wrapper to expose something better to play with.
      inputComponent: _NativeSelectInput.default,
      inputProps: {
        children,
        classes: otherClasses,
        IconComponent,
        variant: fcs.variant,
        type: undefined,
        // We render a select. We can ignore the type provided by the `Input`.
        ...inputProps,
        ...(input ? input.props.inputProps : {})
      },
      ref,
      ...other,
      className: (0, _clsx.default)(classes.root, input.props.className, className)
    })
  });
});
process.env.NODE_ENV !== "production" ? NativeSelect.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The option elements to populate the select with.
   * Can be some `<option>` elements.
   */
  children: _propTypes.default.node,
  /**
   * Override or extend the styles applied to the component.
   * @default {}
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The icon that displays the arrow.
   * @default ArrowDropDownIcon
   */
  IconComponent: _propTypes.default.elementType,
  /**
   * An `Input` element; does not have to be a material-ui specific `Input`.
   * @default <Input />
   */
  input: _propTypes.default.element,
  /**
   * [Attributes](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/select#attributes) applied to the `select` element.
   */
  inputProps: _propTypes.default.object,
  /**
   * Callback fired when a menu item is selected.
   *
   * @param {React.ChangeEvent<HTMLSelectElement>} event The event source of the callback.
   * You can pull out the new value by accessing `event.target.value` (string).
   */
  onChange: _propTypes.default.func,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The `input` value. The DOM API casts this to a string.
   */
  value: _propTypes.default.any,
  /**
   * The variant to use.
   */
  variant: _propTypes.default.oneOf(['filled', 'outlined', 'standard'])
} : void 0;
NativeSelect.muiName = 'Select';
var _default = exports.default = NativeSelect;