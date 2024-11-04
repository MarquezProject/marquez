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
var _colorManipulator = require("@mui/system/colorManipulator");
var _elementTypeAcceptingRef = _interopRequireDefault(require("@mui/utils/elementTypeAcceptingRef"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _isFocusVisible = _interopRequireDefault(require("@mui/utils/isFocusVisible"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _createSimplePaletteValueFilter = _interopRequireDefault(require("../utils/createSimplePaletteValueFilter"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _Typography = _interopRequireDefault(require("../Typography"));
var _linkClasses = _interopRequireWildcard(require("./linkClasses"));
var _getTextDecoration = _interopRequireDefault(require("./getTextDecoration"));
var _jsxRuntime = require("react/jsx-runtime");
const v6Colors = {
  primary: true,
  secondary: true,
  error: true,
  info: true,
  success: true,
  warning: true,
  textPrimary: true,
  textSecondary: true,
  textDisabled: true
};
const useUtilityClasses = ownerState => {
  const {
    classes,
    component,
    focusVisible,
    underline
  } = ownerState;
  const slots = {
    root: ['root', `underline${(0, _capitalize.default)(underline)}`, component === 'button' && 'button', focusVisible && 'focusVisible']
  };
  return (0, _composeClasses.default)(slots, _linkClasses.getLinkUtilityClass, classes);
};
const LinkRoot = (0, _zeroStyled.styled)(_Typography.default, {
  name: 'MuiLink',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[`underline${(0, _capitalize.default)(ownerState.underline)}`], ownerState.component === 'button' && styles.button];
  }
})((0, _memoTheme.default)(({
  theme
}) => {
  return {
    variants: [{
      props: {
        underline: 'none'
      },
      style: {
        textDecoration: 'none'
      }
    }, {
      props: {
        underline: 'hover'
      },
      style: {
        textDecoration: 'none',
        '&:hover': {
          textDecoration: 'underline'
        }
      }
    }, {
      props: {
        underline: 'always'
      },
      style: {
        textDecoration: 'underline',
        '&:hover': {
          textDecorationColor: 'inherit'
        }
      }
    }, {
      props: ({
        underline,
        ownerState
      }) => underline === 'always' && ownerState.color !== 'inherit',
      style: {
        textDecorationColor: 'var(--Link-underlineColor)'
      }
    }, ...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)()).map(([color]) => ({
      props: {
        underline: 'always',
        color
      },
      style: {
        '--Link-underlineColor': theme.vars ? `rgba(${theme.vars.palette[color].mainChannel} / 0.4)` : (0, _colorManipulator.alpha)(theme.palette[color].main, 0.4)
      }
    })), {
      props: {
        underline: 'always',
        color: 'textPrimary'
      },
      style: {
        '--Link-underlineColor': theme.vars ? `rgba(${theme.vars.palette.text.primaryChannel} / 0.4)` : (0, _colorManipulator.alpha)(theme.palette.text.primary, 0.4)
      }
    }, {
      props: {
        underline: 'always',
        color: 'textSecondary'
      },
      style: {
        '--Link-underlineColor': theme.vars ? `rgba(${theme.vars.palette.text.secondaryChannel} / 0.4)` : (0, _colorManipulator.alpha)(theme.palette.text.secondary, 0.4)
      }
    }, {
      props: {
        underline: 'always',
        color: 'textDisabled'
      },
      style: {
        '--Link-underlineColor': (theme.vars || theme).palette.text.disabled
      }
    }, {
      props: {
        component: 'button'
      },
      style: {
        position: 'relative',
        WebkitTapHighlightColor: 'transparent',
        backgroundColor: 'transparent',
        // Reset default value
        // We disable the focus ring for mouse, touch and keyboard users.
        outline: 0,
        border: 0,
        margin: 0,
        // Remove the margin in Safari
        borderRadius: 0,
        padding: 0,
        // Remove the padding in Firefox
        cursor: 'pointer',
        userSelect: 'none',
        verticalAlign: 'middle',
        MozAppearance: 'none',
        // Reset
        WebkitAppearance: 'none',
        // Reset
        '&::-moz-focus-inner': {
          borderStyle: 'none' // Remove Firefox dotted outline.
        },
        [`&.${_linkClasses.default.focusVisible}`]: {
          outline: 'auto'
        }
      }
    }]
  };
}));
const Link = /*#__PURE__*/React.forwardRef(function Link(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiLink'
  });
  const theme = (0, _zeroStyled.useTheme)();
  const {
    className,
    color = 'primary',
    component = 'a',
    onBlur,
    onFocus,
    TypographyClasses,
    underline = 'always',
    variant = 'inherit',
    sx,
    ...other
  } = props;
  const [focusVisible, setFocusVisible] = React.useState(false);
  const handleBlur = event => {
    if (!(0, _isFocusVisible.default)(event.target)) {
      setFocusVisible(false);
    }
    if (onBlur) {
      onBlur(event);
    }
  };
  const handleFocus = event => {
    if ((0, _isFocusVisible.default)(event.target)) {
      setFocusVisible(true);
    }
    if (onFocus) {
      onFocus(event);
    }
  };
  const ownerState = {
    ...props,
    color,
    component,
    focusVisible,
    underline,
    variant
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(LinkRoot, {
    color: color,
    className: (0, _clsx.default)(classes.root, className),
    classes: TypographyClasses,
    component: component,
    onBlur: handleBlur,
    onFocus: handleFocus,
    ref: ref,
    ownerState: ownerState,
    variant: variant,
    ...other,
    sx: [...(v6Colors[color] === undefined ? [{
      color
    }] : []), ...(Array.isArray(sx) ? sx : [sx])],
    style: {
      ...other.style,
      ...(underline === 'always' && color !== 'inherit' && !v6Colors[color] && {
        '--Link-underlineColor': (0, _getTextDecoration.default)({
          theme,
          ownerState
        })
      })
    }
  });
});
process.env.NODE_ENV !== "production" ? Link.propTypes /* remove-proptypes */ = {
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
   * The color of the link.
   * @default 'primary'
   */
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['primary', 'secondary', 'success', 'error', 'info', 'warning', 'textPrimary', 'textSecondary', 'textDisabled']), _propTypes.default.string]),
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _elementTypeAcceptingRef.default,
  /**
   * @ignore
   */
  onBlur: _propTypes.default.func,
  /**
   * @ignore
   */
  onFocus: _propTypes.default.func,
  /**
   * @ignore
   */
  style: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * `classes` prop applied to the [`Typography`](https://mui.com/material-ui/api/typography/) element.
   */
  TypographyClasses: _propTypes.default.object,
  /**
   * Controls when the link should have an underline.
   * @default 'always'
   */
  underline: _propTypes.default.oneOf(['always', 'hover', 'none']),
  /**
   * Applies the theme typography styles.
   * @default 'inherit'
   */
  variant: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['body1', 'body2', 'button', 'caption', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'inherit', 'overline', 'subtitle1', 'subtitle2']), _propTypes.default.string])
} : void 0;
var _default = exports.default = Link;