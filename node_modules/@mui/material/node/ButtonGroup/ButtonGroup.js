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
var _colorManipulator = require("@mui/system/colorManipulator");
var _getValidReactChildren = _interopRequireDefault(require("@mui/utils/getValidReactChildren"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _createSimplePaletteValueFilter = _interopRequireDefault(require("../utils/createSimplePaletteValueFilter"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _buttonGroupClasses = _interopRequireWildcard(require("./buttonGroupClasses"));
var _ButtonGroupContext = _interopRequireDefault(require("./ButtonGroupContext"));
var _ButtonGroupButtonContext = _interopRequireDefault(require("./ButtonGroupButtonContext"));
var _jsxRuntime = require("react/jsx-runtime");
const overridesResolver = (props, styles) => {
  const {
    ownerState
  } = props;
  return [{
    [`& .${_buttonGroupClasses.default.grouped}`]: styles.grouped
  }, {
    [`& .${_buttonGroupClasses.default.grouped}`]: styles[`grouped${(0, _capitalize.default)(ownerState.orientation)}`]
  }, {
    [`& .${_buttonGroupClasses.default.grouped}`]: styles[`grouped${(0, _capitalize.default)(ownerState.variant)}`]
  }, {
    [`& .${_buttonGroupClasses.default.grouped}`]: styles[`grouped${(0, _capitalize.default)(ownerState.variant)}${(0, _capitalize.default)(ownerState.orientation)}`]
  }, {
    [`& .${_buttonGroupClasses.default.grouped}`]: styles[`grouped${(0, _capitalize.default)(ownerState.variant)}${(0, _capitalize.default)(ownerState.color)}`]
  }, {
    [`& .${_buttonGroupClasses.default.firstButton}`]: styles.firstButton
  }, {
    [`& .${_buttonGroupClasses.default.lastButton}`]: styles.lastButton
  }, {
    [`& .${_buttonGroupClasses.default.middleButton}`]: styles.middleButton
  }, styles.root, styles[ownerState.variant], ownerState.disableElevation === true && styles.disableElevation, ownerState.fullWidth && styles.fullWidth, ownerState.orientation === 'vertical' && styles.vertical];
};
const useUtilityClasses = ownerState => {
  const {
    classes,
    color,
    disabled,
    disableElevation,
    fullWidth,
    orientation,
    variant
  } = ownerState;
  const slots = {
    root: ['root', variant, orientation, fullWidth && 'fullWidth', disableElevation && 'disableElevation', `color${(0, _capitalize.default)(color)}`],
    grouped: ['grouped', `grouped${(0, _capitalize.default)(orientation)}`, `grouped${(0, _capitalize.default)(variant)}`, `grouped${(0, _capitalize.default)(variant)}${(0, _capitalize.default)(orientation)}`, `grouped${(0, _capitalize.default)(variant)}${(0, _capitalize.default)(color)}`, disabled && 'disabled'],
    firstButton: ['firstButton'],
    lastButton: ['lastButton'],
    middleButton: ['middleButton']
  };
  return (0, _composeClasses.default)(slots, _buttonGroupClasses.getButtonGroupUtilityClass, classes);
};
const ButtonGroupRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiButtonGroup',
  slot: 'Root',
  overridesResolver
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'inline-flex',
  borderRadius: (theme.vars || theme).shape.borderRadius,
  variants: [{
    props: {
      variant: 'contained'
    },
    style: {
      boxShadow: (theme.vars || theme).shadows[2]
    }
  }, {
    props: {
      disableElevation: true
    },
    style: {
      boxShadow: 'none'
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
      orientation: 'vertical'
    },
    style: {
      flexDirection: 'column',
      [`& .${_buttonGroupClasses.default.lastButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderTopRightRadius: 0,
        borderTopLeftRadius: 0
      },
      [`& .${_buttonGroupClasses.default.firstButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderBottomRightRadius: 0,
        borderBottomLeftRadius: 0
      }
    }
  }, {
    props: {
      orientation: 'horizontal'
    },
    style: {
      [`& .${_buttonGroupClasses.default.firstButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderTopRightRadius: 0,
        borderBottomRightRadius: 0
      },
      [`& .${_buttonGroupClasses.default.lastButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderTopLeftRadius: 0,
        borderBottomLeftRadius: 0
      }
    }
  }, {
    props: {
      variant: 'text',
      orientation: 'horizontal'
    },
    style: {
      [`& .${_buttonGroupClasses.default.firstButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderRight: theme.vars ? `1px solid rgba(${theme.vars.palette.common.onBackgroundChannel} / 0.23)` : `1px solid ${theme.palette.mode === 'light' ? 'rgba(0, 0, 0, 0.23)' : 'rgba(255, 255, 255, 0.23)'}`,
        [`&.${_buttonGroupClasses.default.disabled}`]: {
          borderRight: `1px solid ${(theme.vars || theme).palette.action.disabled}`
        }
      }
    }
  }, {
    props: {
      variant: 'text',
      orientation: 'vertical'
    },
    style: {
      [`& .${_buttonGroupClasses.default.firstButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderBottom: theme.vars ? `1px solid rgba(${theme.vars.palette.common.onBackgroundChannel} / 0.23)` : `1px solid ${theme.palette.mode === 'light' ? 'rgba(0, 0, 0, 0.23)' : 'rgba(255, 255, 255, 0.23)'}`,
        [`&.${_buttonGroupClasses.default.disabled}`]: {
          borderBottom: `1px solid ${(theme.vars || theme).palette.action.disabled}`
        }
      }
    }
  }, ...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)()).flatMap(([color]) => [{
    props: {
      variant: 'text',
      color
    },
    style: {
      [`& .${_buttonGroupClasses.default.firstButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderColor: theme.vars ? `rgba(${theme.vars.palette[color].mainChannel} / 0.5)` : (0, _colorManipulator.alpha)(theme.palette[color].main, 0.5)
      }
    }
  }]), {
    props: {
      variant: 'outlined',
      orientation: 'horizontal'
    },
    style: {
      [`& .${_buttonGroupClasses.default.firstButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderRightColor: 'transparent',
        '&:hover': {
          borderRightColor: 'currentColor'
        }
      },
      [`& .${_buttonGroupClasses.default.lastButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        marginLeft: -1
      }
    }
  }, {
    props: {
      variant: 'outlined',
      orientation: 'vertical'
    },
    style: {
      [`& .${_buttonGroupClasses.default.firstButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderBottomColor: 'transparent',
        '&:hover': {
          borderBottomColor: 'currentColor'
        }
      },
      [`& .${_buttonGroupClasses.default.lastButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        marginTop: -1
      }
    }
  }, {
    props: {
      variant: 'contained',
      orientation: 'horizontal'
    },
    style: {
      [`& .${_buttonGroupClasses.default.firstButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderRight: `1px solid ${(theme.vars || theme).palette.grey[400]}`,
        [`&.${_buttonGroupClasses.default.disabled}`]: {
          borderRight: `1px solid ${(theme.vars || theme).palette.action.disabled}`
        }
      }
    }
  }, {
    props: {
      variant: 'contained',
      orientation: 'vertical'
    },
    style: {
      [`& .${_buttonGroupClasses.default.firstButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderBottom: `1px solid ${(theme.vars || theme).palette.grey[400]}`,
        [`&.${_buttonGroupClasses.default.disabled}`]: {
          borderBottom: `1px solid ${(theme.vars || theme).palette.action.disabled}`
        }
      }
    }
  }, ...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)(['dark'])).map(([color]) => ({
    props: {
      variant: 'contained',
      color
    },
    style: {
      [`& .${_buttonGroupClasses.default.firstButton},& .${_buttonGroupClasses.default.middleButton}`]: {
        borderColor: (theme.vars || theme).palette[color].dark
      }
    }
  }))],
  [`& .${_buttonGroupClasses.default.grouped}`]: {
    minWidth: 40,
    boxShadow: 'none',
    props: {
      variant: 'contained'
    },
    style: {
      '&:hover': {
        boxShadow: 'none'
      }
    }
  }
})));
const ButtonGroup = /*#__PURE__*/React.forwardRef(function ButtonGroup(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiButtonGroup'
  });
  const {
    children,
    className,
    color = 'primary',
    component = 'div',
    disabled = false,
    disableElevation = false,
    disableFocusRipple = false,
    disableRipple = false,
    fullWidth = false,
    orientation = 'horizontal',
    size = 'medium',
    variant = 'outlined',
    ...other
  } = props;
  const ownerState = {
    ...props,
    color,
    component,
    disabled,
    disableElevation,
    disableFocusRipple,
    disableRipple,
    fullWidth,
    orientation,
    size,
    variant
  };
  const classes = useUtilityClasses(ownerState);
  const context = React.useMemo(() => ({
    className: classes.grouped,
    color,
    disabled,
    disableElevation,
    disableFocusRipple,
    disableRipple,
    fullWidth,
    size,
    variant
  }), [color, disabled, disableElevation, disableFocusRipple, disableRipple, fullWidth, size, variant, classes.grouped]);
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
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(ButtonGroupRoot, {
    as: component,
    role: "group",
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(_ButtonGroupContext.default.Provider, {
      value: context,
      children: validChildren.map((child, index) => {
        return /*#__PURE__*/(0, _jsxRuntime.jsx)(_ButtonGroupButtonContext.default.Provider, {
          value: getButtonPositionClassName(index),
          children: child
        }, index);
      })
    })
  });
});
process.env.NODE_ENV !== "production" ? ButtonGroup.propTypes /* remove-proptypes */ = {
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
   * @default 'primary'
   */
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['inherit', 'primary', 'secondary', 'error', 'info', 'success', 'warning']), _propTypes.default.string]),
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * If `true`, the component is disabled.
   * @default false
   */
  disabled: _propTypes.default.bool,
  /**
   * If `true`, no elevation is used.
   * @default false
   */
  disableElevation: _propTypes.default.bool,
  /**
   * If `true`, the button keyboard focus ripple is disabled.
   * @default false
   */
  disableFocusRipple: _propTypes.default.bool,
  /**
   * If `true`, the button ripple effect is disabled.
   * @default false
   */
  disableRipple: _propTypes.default.bool,
  /**
   * If `true`, the buttons will take up the full width of its container.
   * @default false
   */
  fullWidth: _propTypes.default.bool,
  /**
   * The component orientation (layout flow direction).
   * @default 'horizontal'
   */
  orientation: _propTypes.default.oneOf(['horizontal', 'vertical']),
  /**
   * The size of the component.
   * `small` is equivalent to the dense button styling.
   * @default 'medium'
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['small', 'medium', 'large']), _propTypes.default.string]),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The variant to use.
   * @default 'outlined'
   */
  variant: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['contained', 'outlined', 'text']), _propTypes.default.string])
} : void 0;
var _default = exports.default = ButtonGroup;