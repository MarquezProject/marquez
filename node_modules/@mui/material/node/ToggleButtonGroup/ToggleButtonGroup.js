"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _reactIs = require("react-is");
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _getValidReactChildren = _interopRequireDefault(require("@mui/utils/getValidReactChildren"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _toggleButtonGroupClasses = _interopRequireWildcard(require("./toggleButtonGroupClasses"));
var _ToggleButtonGroupContext = _interopRequireDefault(require("./ToggleButtonGroupContext"));
var _ToggleButtonGroupButtonContext = _interopRequireDefault(require("./ToggleButtonGroupButtonContext"));
var _toggleButtonClasses = _interopRequireDefault(require("../ToggleButton/toggleButtonClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    orientation,
    fullWidth,
    disabled
  } = ownerState;
  const slots = {
    root: ['root', orientation, fullWidth && 'fullWidth'],
    grouped: ['grouped', `grouped${(0, _capitalize.default)(orientation)}`, disabled && 'disabled'],
    firstButton: ['firstButton'],
    lastButton: ['lastButton'],
    middleButton: ['middleButton']
  };
  return (0, _composeClasses.default)(slots, _toggleButtonGroupClasses.getToggleButtonGroupUtilityClass, classes);
};
const ToggleButtonGroupRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiToggleButtonGroup',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [{
      [`& .${_toggleButtonGroupClasses.default.grouped}`]: styles.grouped
    }, {
      [`& .${_toggleButtonGroupClasses.default.grouped}`]: styles[`grouped${(0, _capitalize.default)(ownerState.orientation)}`]
    }, {
      [`& .${_toggleButtonGroupClasses.default.firstButton}`]: styles.firstButton
    }, {
      [`& .${_toggleButtonGroupClasses.default.lastButton}`]: styles.lastButton
    }, {
      [`& .${_toggleButtonGroupClasses.default.middleButton}`]: styles.middleButton
    }, styles.root, ownerState.orientation === 'vertical' && styles.vertical, ownerState.fullWidth && styles.fullWidth];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'inline-flex',
  borderRadius: (theme.vars || theme).shape.borderRadius,
  variants: [{
    props: {
      orientation: 'vertical'
    },
    style: {
      flexDirection: 'column',
      [`& .${_toggleButtonGroupClasses.default.grouped}`]: {
        [`&.${_toggleButtonGroupClasses.default.selected} + .${_toggleButtonGroupClasses.default.grouped}.${_toggleButtonGroupClasses.default.selected}`]: {
          borderTop: 0,
          marginTop: 0
        }
      },
      [`& .${_toggleButtonGroupClasses.default.firstButton},& .${_toggleButtonGroupClasses.default.middleButton}`]: {
        borderBottomLeftRadius: 0,
        borderBottomRightRadius: 0
      },
      [`& .${_toggleButtonGroupClasses.default.lastButton},& .${_toggleButtonGroupClasses.default.middleButton}`]: {
        marginTop: -1,
        borderTop: '1px solid transparent',
        borderTopLeftRadius: 0,
        borderTopRightRadius: 0
      },
      [`& .${_toggleButtonGroupClasses.default.lastButton}.${_toggleButtonClasses.default.disabled},& .${_toggleButtonGroupClasses.default.middleButton}.${_toggleButtonClasses.default.disabled}`]: {
        borderTop: '1px solid transparent'
      }
    }
  }, {
    props: {
      fullWidth: true
    },
    style: {
      width: '100%'
    }
  }, {
    props: {
      orientation: 'horizontal'
    },
    style: {
      [`& .${_toggleButtonGroupClasses.default.grouped}`]: {
        [`&.${_toggleButtonGroupClasses.default.selected} + .${_toggleButtonGroupClasses.default.grouped}.${_toggleButtonGroupClasses.default.selected}`]: {
          borderLeft: 0,
          marginLeft: 0
        }
      },
      [`& .${_toggleButtonGroupClasses.default.firstButton},& .${_toggleButtonGroupClasses.default.middleButton}`]: {
        borderTopRightRadius: 0,
        borderBottomRightRadius: 0
      },
      [`& .${_toggleButtonGroupClasses.default.lastButton},& .${_toggleButtonGroupClasses.default.middleButton}`]: {
        marginLeft: -1,
        borderLeft: '1px solid transparent',
        borderTopLeftRadius: 0,
        borderBottomLeftRadius: 0
      },
      [`& .${_toggleButtonGroupClasses.default.lastButton}.${_toggleButtonClasses.default.disabled},& .${_toggleButtonGroupClasses.default.middleButton}.${_toggleButtonClasses.default.disabled}`]: {
        borderLeft: '1px solid transparent'
      }
    }
  }]
})));
const ToggleButtonGroup = /*#__PURE__*/React.forwardRef(function ToggleButtonGroup(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiToggleButtonGroup'
  });
  const {
    children,
    className,
    color = 'standard',
    disabled = false,
    exclusive = false,
    fullWidth = false,
    onChange,
    orientation = 'horizontal',
    size = 'medium',
    value,
    ...other
  } = props;
  const ownerState = {
    ...props,
    disabled,
    fullWidth,
    orientation,
    size
  };
  const classes = useUtilityClasses(ownerState);
  const handleChange = React.useCallback((event, buttonValue) => {
    if (!onChange) {
      return;
    }
    const index = value && value.indexOf(buttonValue);
    let newValue;
    if (value && index >= 0) {
      newValue = value.slice();
      newValue.splice(index, 1);
    } else {
      newValue = value ? value.concat(buttonValue) : [buttonValue];
    }
    onChange(event, newValue);
  }, [onChange, value]);
  const handleExclusiveChange = React.useCallback((event, buttonValue) => {
    if (!onChange) {
      return;
    }
    onChange(event, value === buttonValue ? null : buttonValue);
  }, [onChange, value]);
  const context = React.useMemo(() => ({
    className: classes.grouped,
    onChange: exclusive ? handleExclusiveChange : handleChange,
    value,
    size,
    fullWidth,
    color,
    disabled
  }), [classes.grouped, exclusive, handleExclusiveChange, handleChange, value, size, fullWidth, color, disabled]);
  const validChildren = (0, _getValidReactChildren.default)(children);
  const childrenCount = validChildren.length;
  const getButtonPositionClassName = index => {
    const isFirstButton = index === 0;
    const isLastButton = index === childrenCount - 1;
    if (isFirstButton && isLastButton) {
      return '';
    }
    if (isFirstButton) {
      return classes.firstButton;
    }
    if (isLastButton) {
      return classes.lastButton;
    }
    return classes.middleButton;
  };
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(ToggleButtonGroupRoot, {
    role: "group",
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(_ToggleButtonGroupContext.default.Provider, {
      value: context,
      children: validChildren.map((child, index) => {
        if (process.env.NODE_ENV !== 'production') {
          if ((0, _reactIs.isFragment)(child)) {
            console.error(["MUI: The ToggleButtonGroup component doesn't accept a Fragment as a child.", 'Consider providing an array instead.'].join('\n'));
          }
        }
        return /*#__PURE__*/(0, _jsxRuntime.jsx)(_ToggleButtonGroupButtonContext.default.Provider, {
          value: getButtonPositionClassName(index),
          children: child
        }, index);
      })
    })
  });
});
process.env.NODE_ENV !== "production" ? ToggleButtonGroup.propTypes /* remove-proptypes */ = {
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
   * The color of the button when it is selected.
   * It supports both default and custom theme colors, which can be added as shown in the
   * [palette customization guide](https://mui.com/material-ui/customization/palette/#custom-colors).
   * @default 'standard'
   */
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['standard', 'primary', 'secondary', 'error', 'info', 'success', 'warning']), _propTypes.default.string]),
  /**
   * If `true`, the component is disabled. This implies that all ToggleButton children will be disabled.
   * @default false
   */
  disabled: _propTypes.default.bool,
  /**
   * If `true`, only allow one of the child ToggleButton values to be selected.
   * @default false
   */
  exclusive: _propTypes.default.bool,
  /**
   * If `true`, the button group will take up the full width of its container.
   * @default false
   */
  fullWidth: _propTypes.default.bool,
  /**
   * Callback fired when the value changes.
   *
   * @param {React.MouseEvent<HTMLElement>} event The event source of the callback.
   * @param {any} value of the selected buttons. When `exclusive` is true
   * this is a single value; when false an array of selected values. If no value
   * is selected and `exclusive` is true the value is null; when false an empty array.
   */
  onChange: _propTypes.default.func,
  /**
   * The component orientation (layout flow direction).
   * @default 'horizontal'
   */
  orientation: _propTypes.default.oneOf(['horizontal', 'vertical']),
  /**
   * The size of the component.
   * @default 'medium'
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['small', 'medium', 'large']), _propTypes.default.string]),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The currently selected value within the group or an array of selected
   * values when `exclusive` is false.
   *
   * The value must have reference equality with the option in order to be selected.
   */
  value: _propTypes.default.any
} : void 0;
var _default = exports.default = ToggleButtonGroup;