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
var _refType = _interopRequireDefault(require("@mui/utils/refType"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _colorManipulator = require("@mui/system/colorManipulator");
var _SwitchBase = _interopRequireDefault(require("../internal/SwitchBase"));
var _CheckBoxOutlineBlank = _interopRequireDefault(require("../internal/svg-icons/CheckBoxOutlineBlank"));
var _CheckBox = _interopRequireDefault(require("../internal/svg-icons/CheckBox"));
var _IndeterminateCheckBox = _interopRequireDefault(require("../internal/svg-icons/IndeterminateCheckBox"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _rootShouldForwardProp = _interopRequireDefault(require("../styles/rootShouldForwardProp"));
var _checkboxClasses = _interopRequireWildcard(require("./checkboxClasses"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _createSimplePaletteValueFilter = _interopRequireDefault(require("../utils/createSimplePaletteValueFilter"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    indeterminate,
    color,
    size
  } = ownerState;
  const slots = {
    root: ['root', indeterminate && 'indeterminate', `color${(0, _capitalize.default)(color)}`, `size${(0, _capitalize.default)(size)}`]
  };
  const composedClasses = (0, _composeClasses.default)(slots, _checkboxClasses.getCheckboxUtilityClass, classes);
  return {
    ...classes,
    // forward the disabled and checked classes to the SwitchBase
    ...composedClasses
  };
};
const CheckboxRoot = (0, _zeroStyled.styled)(_SwitchBase.default, {
  shouldForwardProp: prop => (0, _rootShouldForwardProp.default)(prop) || prop === 'classes',
  name: 'MuiCheckbox',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.indeterminate && styles.indeterminate, styles[`size${(0, _capitalize.default)(ownerState.size)}`], ownerState.color !== 'default' && styles[`color${(0, _capitalize.default)(ownerState.color)}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  color: (theme.vars || theme).palette.text.secondary,
  variants: [{
    props: {
      color: 'default',
      disableRipple: false
    },
    style: {
      '&:hover': {
        backgroundColor: theme.vars ? `rgba(${theme.vars.palette.action.activeChannel} / ${theme.vars.palette.action.hoverOpacity})` : (0, _colorManipulator.alpha)(theme.palette.action.active, theme.palette.action.hoverOpacity)
      }
    }
  }, ...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)()).map(([color]) => ({
    props: {
      color,
      disableRipple: false
    },
    style: {
      '&:hover': {
        backgroundColor: theme.vars ? `rgba(${theme.vars.palette[color].mainChannel} / ${theme.vars.palette.action.hoverOpacity})` : (0, _colorManipulator.alpha)(theme.palette[color].main, theme.palette.action.hoverOpacity)
      }
    }
  })), ...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)()).map(([color]) => ({
    props: {
      color
    },
    style: {
      [`&.${_checkboxClasses.default.checked}, &.${_checkboxClasses.default.indeterminate}`]: {
        color: (theme.vars || theme).palette[color].main
      },
      [`&.${_checkboxClasses.default.disabled}`]: {
        color: (theme.vars || theme).palette.action.disabled
      }
    }
  })), {
    // Should be last to override other colors
    props: {
      disableRipple: false
    },
    style: {
      // Reset on touch devices, it doesn't add specificity
      '&:hover': {
        '@media (hover: none)': {
          backgroundColor: 'transparent'
        }
      }
    }
  }]
})));
const defaultCheckedIcon = /*#__PURE__*/(0, _jsxRuntime.jsx)(_CheckBox.default, {});
const defaultIcon = /*#__PURE__*/(0, _jsxRuntime.jsx)(_CheckBoxOutlineBlank.default, {});
const defaultIndeterminateIcon = /*#__PURE__*/(0, _jsxRuntime.jsx)(_IndeterminateCheckBox.default, {});
const Checkbox = /*#__PURE__*/React.forwardRef(function Checkbox(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiCheckbox'
  });
  const {
    checkedIcon = defaultCheckedIcon,
    color = 'primary',
    icon: iconProp = defaultIcon,
    indeterminate = false,
    indeterminateIcon: indeterminateIconProp = defaultIndeterminateIcon,
    inputProps,
    size = 'medium',
    disableRipple = false,
    className,
    ...other
  } = props;
  const icon = indeterminate ? indeterminateIconProp : iconProp;
  const indeterminateIcon = indeterminate ? indeterminateIconProp : checkedIcon;
  const ownerState = {
    ...props,
    disableRipple,
    color,
    indeterminate,
    size
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(CheckboxRoot, {
    type: "checkbox",
    inputProps: {
      'data-indeterminate': indeterminate,
      ...inputProps
    },
    icon: /*#__PURE__*/React.cloneElement(icon, {
      fontSize: icon.props.fontSize ?? size
    }),
    checkedIcon: /*#__PURE__*/React.cloneElement(indeterminateIcon, {
      fontSize: indeterminateIcon.props.fontSize ?? size
    }),
    ownerState: ownerState,
    ref: ref,
    className: (0, _clsx.default)(classes.root, className),
    disableRipple: disableRipple,
    ...other,
    classes: classes
  });
});
process.env.NODE_ENV !== "production" ? Checkbox.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * If `true`, the component is checked.
   */
  checked: _propTypes.default.bool,
  /**
   * The icon to display when the component is checked.
   * @default <CheckBoxIcon />
   */
  checkedIcon: _propTypes.default.node,
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
   * @default 'primary'
   */
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['default', 'primary', 'secondary', 'error', 'info', 'success', 'warning']), _propTypes.default.string]),
  /**
   * The default checked state. Use when the component is not controlled.
   */
  defaultChecked: _propTypes.default.bool,
  /**
   * If `true`, the component is disabled.
   * @default false
   */
  disabled: _propTypes.default.bool,
  /**
   * If `true`, the ripple effect is disabled.
   * @default false
   */
  disableRipple: _propTypes.default.bool,
  /**
   * The icon to display when the component is unchecked.
   * @default <CheckBoxOutlineBlankIcon />
   */
  icon: _propTypes.default.node,
  /**
   * The id of the `input` element.
   */
  id: _propTypes.default.string,
  /**
   * If `true`, the component appears indeterminate.
   * This does not set the native input element to indeterminate due
   * to inconsistent behavior across browsers.
   * However, we set a `data-indeterminate` attribute on the `input`.
   * @default false
   */
  indeterminate: _propTypes.default.bool,
  /**
   * The icon to display when the component is indeterminate.
   * @default <IndeterminateCheckBoxIcon />
   */
  indeterminateIcon: _propTypes.default.node,
  /**
   * [Attributes](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input#Attributes) applied to the `input` element.
   */
  inputProps: _propTypes.default.object,
  /**
   * Pass a ref to the `input` element.
   */
  inputRef: _refType.default,
  /**
   * Callback fired when the state is changed.
   *
   * @param {React.ChangeEvent<HTMLInputElement>} event The event source of the callback.
   * You can pull out the new checked state by accessing `event.target.checked` (boolean).
   */
  onChange: _propTypes.default.func,
  /**
   * If `true`, the `input` element is required.
   * @default false
   */
  required: _propTypes.default.bool,
  /**
   * The size of the component.
   * `small` is equivalent to the dense checkbox styling.
   * @default 'medium'
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['medium', 'small']), _propTypes.default.string]),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The value of the component. The DOM API casts this to a string.
   * The browser uses "on" as the default value.
   */
  value: _propTypes.default.any
} : void 0;
var _default = exports.default = Checkbox;