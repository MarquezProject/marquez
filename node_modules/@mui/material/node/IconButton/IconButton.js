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
var _chainPropTypes = _interopRequireDefault(require("@mui/utils/chainPropTypes"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _colorManipulator = require("@mui/system/colorManipulator");
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _createSimplePaletteValueFilter = _interopRequireDefault(require("../utils/createSimplePaletteValueFilter"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _ButtonBase = _interopRequireDefault(require("../ButtonBase"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _iconButtonClasses = _interopRequireWildcard(require("./iconButtonClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    disabled,
    color,
    edge,
    size
  } = ownerState;
  const slots = {
    root: ['root', disabled && 'disabled', color !== 'default' && `color${(0, _capitalize.default)(color)}`, edge && `edge${(0, _capitalize.default)(edge)}`, `size${(0, _capitalize.default)(size)}`]
  };
  return (0, _composeClasses.default)(slots, _iconButtonClasses.getIconButtonUtilityClass, classes);
};
const IconButtonRoot = (0, _zeroStyled.styled)(_ButtonBase.default, {
  name: 'MuiIconButton',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.color !== 'default' && styles[`color${(0, _capitalize.default)(ownerState.color)}`], ownerState.edge && styles[`edge${(0, _capitalize.default)(ownerState.edge)}`], styles[`size${(0, _capitalize.default)(ownerState.size)}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  textAlign: 'center',
  flex: '0 0 auto',
  fontSize: theme.typography.pxToRem(24),
  padding: 8,
  borderRadius: '50%',
  color: (theme.vars || theme).palette.action.active,
  transition: theme.transitions.create('background-color', {
    duration: theme.transitions.duration.shortest
  }),
  variants: [{
    props: props => !props.disableRipple,
    style: {
      '--IconButton-hoverBg': theme.vars ? `rgba(${theme.vars.palette.action.activeChannel} / ${theme.vars.palette.action.hoverOpacity})` : (0, _colorManipulator.alpha)(theme.palette.action.active, theme.palette.action.hoverOpacity),
      '&:hover': {
        backgroundColor: 'var(--IconButton-hoverBg)',
        // Reset on touch devices, it doesn't add specificity
        '@media (hover: none)': {
          backgroundColor: 'transparent'
        }
      }
    }
  }, {
    props: {
      edge: 'start'
    },
    style: {
      marginLeft: -12
    }
  }, {
    props: {
      edge: 'start',
      size: 'small'
    },
    style: {
      marginLeft: -3
    }
  }, {
    props: {
      edge: 'end'
    },
    style: {
      marginRight: -12
    }
  }, {
    props: {
      edge: 'end',
      size: 'small'
    },
    style: {
      marginRight: -3
    }
  }]
})), (0, _memoTheme.default)(({
  theme
}) => ({
  variants: [{
    props: {
      color: 'inherit'
    },
    style: {
      color: 'inherit'
    }
  }, ...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)()) // check all the used fields in the style below
  .map(([color]) => ({
    props: {
      color
    },
    style: {
      color: (theme.vars || theme).palette[color].main
    }
  })), ...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)()) // check all the used fields in the style below
  .map(([color]) => ({
    props: {
      color
    },
    style: {
      '--IconButton-hoverBg': theme.vars ? `rgba(${(theme.vars || theme).palette[color].mainChannel} / ${theme.vars.palette.action.hoverOpacity})` : (0, _colorManipulator.alpha)((theme.vars || theme).palette[color].main, theme.palette.action.hoverOpacity)
    }
  })), {
    props: {
      size: 'small'
    },
    style: {
      padding: 5,
      fontSize: theme.typography.pxToRem(18)
    }
  }, {
    props: {
      size: 'large'
    },
    style: {
      padding: 12,
      fontSize: theme.typography.pxToRem(28)
    }
  }],
  [`&.${_iconButtonClasses.default.disabled}`]: {
    backgroundColor: 'transparent',
    color: (theme.vars || theme).palette.action.disabled
  }
})));

/**
 * Refer to the [Icons](/material-ui/icons/) section of the documentation
 * regarding the available icon options.
 */
const IconButton = /*#__PURE__*/React.forwardRef(function IconButton(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiIconButton'
  });
  const {
    edge = false,
    children,
    className,
    color = 'default',
    disabled = false,
    disableFocusRipple = false,
    size = 'medium',
    ...other
  } = props;
  const ownerState = {
    ...props,
    edge,
    color,
    disabled,
    disableFocusRipple,
    size
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(IconButtonRoot, {
    className: (0, _clsx.default)(classes.root, className),
    centerRipple: true,
    focusRipple: !disableFocusRipple,
    disabled: disabled,
    ref: ref,
    ...other,
    ownerState: ownerState,
    children: children
  });
});
process.env.NODE_ENV !== "production" ? IconButton.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The icon to display.
   */
  children: (0, _chainPropTypes.default)(_propTypes.default.node, props => {
    const found = React.Children.toArray(props.children).some(child => /*#__PURE__*/React.isValidElement(child) && child.props.onClick);
    if (found) {
      return new Error(['MUI: You are providing an onClick event listener to a child of a button element.', 'Prefer applying it to the IconButton directly.', 'This guarantees that the whole <button> will be responsive to click events.'].join('\n'));
    }
    return null;
  }),
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
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['inherit', 'default', 'primary', 'secondary', 'error', 'info', 'success', 'warning']), _propTypes.default.string]),
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
   *
   * ⚠️ Without a ripple there is no styling for :focus-visible by default. Be sure
   * to highlight the element by applying separate styles with the `.Mui-focusVisible` class.
   * @default false
   */
  disableRipple: _propTypes.default.bool,
  /**
   * If given, uses a negative margin to counteract the padding on one
   * side (this is often helpful for aligning the left or right
   * side of the icon with content above or below, without ruining the border
   * size and shape).
   * @default false
   */
  edge: _propTypes.default.oneOf(['end', 'start', false]),
  /**
   * The size of the component.
   * `small` is equivalent to the dense button styling.
   * @default 'medium'
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['small', 'medium', 'large']), _propTypes.default.string]),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = IconButton;