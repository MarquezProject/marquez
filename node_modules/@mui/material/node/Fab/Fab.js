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
var _ButtonBase = _interopRequireDefault(require("../ButtonBase"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _fabClasses = _interopRequireWildcard(require("./fabClasses"));
var _rootShouldForwardProp = _interopRequireDefault(require("../styles/rootShouldForwardProp"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _createSimplePaletteValueFilter = _interopRequireDefault(require("../utils/createSimplePaletteValueFilter"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    color,
    variant,
    classes,
    size
  } = ownerState;
  const slots = {
    root: ['root', variant, `size${(0, _capitalize.default)(size)}`, color === 'inherit' ? 'colorInherit' : color]
  };
  const composedClasses = (0, _composeClasses.default)(slots, _fabClasses.getFabUtilityClass, classes);
  return {
    ...classes,
    // forward the focused, disabled, etc. classes to the ButtonBase
    ...composedClasses
  };
};
const FabRoot = (0, _zeroStyled.styled)(_ButtonBase.default, {
  name: 'MuiFab',
  slot: 'Root',
  shouldForwardProp: prop => (0, _rootShouldForwardProp.default)(prop) || prop === 'classes',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[ownerState.variant], styles[`size${(0, _capitalize.default)(ownerState.size)}`], ownerState.color === 'inherit' && styles.colorInherit, styles[(0, _capitalize.default)(ownerState.size)], styles[ownerState.color]];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  ...theme.typography.button,
  minHeight: 36,
  transition: theme.transitions.create(['background-color', 'box-shadow', 'border-color'], {
    duration: theme.transitions.duration.short
  }),
  borderRadius: '50%',
  padding: 0,
  minWidth: 0,
  width: 56,
  height: 56,
  zIndex: (theme.vars || theme).zIndex.fab,
  boxShadow: (theme.vars || theme).shadows[6],
  '&:active': {
    boxShadow: (theme.vars || theme).shadows[12]
  },
  color: theme.vars ? theme.vars.palette.text.primary : theme.palette.getContrastText?.(theme.palette.grey[300]),
  backgroundColor: (theme.vars || theme).palette.grey[300],
  '&:hover': {
    backgroundColor: (theme.vars || theme).palette.grey.A100,
    // Reset on touch devices, it doesn't add specificity
    '@media (hover: none)': {
      backgroundColor: (theme.vars || theme).palette.grey[300]
    },
    textDecoration: 'none'
  },
  [`&.${_fabClasses.default.focusVisible}`]: {
    boxShadow: (theme.vars || theme).shadows[6]
  },
  variants: [{
    props: {
      size: 'small'
    },
    style: {
      width: 40,
      height: 40
    }
  }, {
    props: {
      size: 'medium'
    },
    style: {
      width: 48,
      height: 48
    }
  }, {
    props: {
      variant: 'extended'
    },
    style: {
      borderRadius: 48 / 2,
      padding: '0 16px',
      width: 'auto',
      minHeight: 'auto',
      minWidth: 48,
      height: 48
    }
  }, {
    props: {
      variant: 'extended',
      size: 'small'
    },
    style: {
      width: 'auto',
      padding: '0 8px',
      borderRadius: 34 / 2,
      minWidth: 34,
      height: 34
    }
  }, {
    props: {
      variant: 'extended',
      size: 'medium'
    },
    style: {
      width: 'auto',
      padding: '0 16px',
      borderRadius: 40 / 2,
      minWidth: 40,
      height: 40
    }
  }, {
    props: {
      color: 'inherit'
    },
    style: {
      color: 'inherit'
    }
  }]
})), (0, _memoTheme.default)(({
  theme
}) => ({
  variants: [...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)(['dark', 'contrastText'])) // check all the used fields in the style below
  .map(([color]) => ({
    props: {
      color
    },
    style: {
      color: (theme.vars || theme).palette[color].contrastText,
      backgroundColor: (theme.vars || theme).palette[color].main,
      '&:hover': {
        backgroundColor: (theme.vars || theme).palette[color].dark,
        // Reset on touch devices, it doesn't add specificity
        '@media (hover: none)': {
          backgroundColor: (theme.vars || theme).palette[color].main
        }
      }
    }
  }))]
})), (0, _memoTheme.default)(({
  theme
}) => ({
  [`&.${_fabClasses.default.disabled}`]: {
    color: (theme.vars || theme).palette.action.disabled,
    boxShadow: (theme.vars || theme).shadows[0],
    backgroundColor: (theme.vars || theme).palette.action.disabledBackground
  }
})));
const Fab = /*#__PURE__*/React.forwardRef(function Fab(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiFab'
  });
  const {
    children,
    className,
    color = 'default',
    component = 'button',
    disabled = false,
    disableFocusRipple = false,
    focusVisibleClassName,
    size = 'large',
    variant = 'circular',
    ...other
  } = props;
  const ownerState = {
    ...props,
    color,
    component,
    disabled,
    disableFocusRipple,
    size,
    variant
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(FabRoot, {
    className: (0, _clsx.default)(classes.root, className),
    component: component,
    disabled: disabled,
    focusRipple: !disableFocusRipple,
    focusVisibleClassName: (0, _clsx.default)(classes.focusVisible, focusVisibleClassName),
    ownerState: ownerState,
    ref: ref,
    ...other,
    classes: classes,
    children: children
  });
});
process.env.NODE_ENV !== "production" ? Fab.propTypes /* remove-proptypes */ = {
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
   * @default 'default'
   */
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['default', 'error', 'info', 'inherit', 'primary', 'secondary', 'success', 'warning']), _propTypes.default.string]),
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
   * If `true`, the  keyboard focus ripple is disabled.
   * @default false
   */
  disableFocusRipple: _propTypes.default.bool,
  /**
   * If `true`, the ripple effect is disabled.
   */
  disableRipple: _propTypes.default.bool,
  /**
   * @ignore
   */
  focusVisibleClassName: _propTypes.default.string,
  /**
   * The URL to link to when the button is clicked.
   * If defined, an `a` element will be used as the root node.
   */
  href: _propTypes.default.string,
  /**
   * The size of the component.
   * `small` is equivalent to the dense button styling.
   * @default 'large'
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['small', 'medium', 'large']), _propTypes.default.string]),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The variant to use.
   * @default 'circular'
   */
  variant: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['circular', 'extended']), _propTypes.default.string])
} : void 0;
var _default = exports.default = Fab;