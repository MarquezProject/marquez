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
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _createSimplePaletteValueFilter = _interopRequireDefault(require("../utils/createSimplePaletteValueFilter"));
var _Paper = _interopRequireDefault(require("../Paper"));
var _appBarClasses = require("./appBarClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    color,
    position,
    classes
  } = ownerState;
  const slots = {
    root: ['root', `color${(0, _capitalize.default)(color)}`, `position${(0, _capitalize.default)(position)}`]
  };
  return (0, _composeClasses.default)(slots, _appBarClasses.getAppBarUtilityClass, classes);
};

// var2 is the fallback.
// Ex. var1: 'var(--a)', var2: 'var(--b)'; return: 'var(--a, var(--b))'
const joinVars = (var1, var2) => var1 ? `${var1?.replace(')', '')}, ${var2})` : var2;
const AppBarRoot = (0, _zeroStyled.styled)(_Paper.default, {
  name: 'MuiAppBar',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[`position${(0, _capitalize.default)(ownerState.position)}`], styles[`color${(0, _capitalize.default)(ownerState.color)}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'flex',
  flexDirection: 'column',
  width: '100%',
  boxSizing: 'border-box',
  // Prevent padding issue with the Modal and fixed positioned AppBar.
  flexShrink: 0,
  variants: [{
    props: {
      position: 'fixed'
    },
    style: {
      position: 'fixed',
      zIndex: (theme.vars || theme).zIndex.appBar,
      top: 0,
      left: 'auto',
      right: 0,
      '@media print': {
        // Prevent the app bar to be visible on each printed page.
        position: 'absolute'
      }
    }
  }, {
    props: {
      position: 'absolute'
    },
    style: {
      position: 'absolute',
      zIndex: (theme.vars || theme).zIndex.appBar,
      top: 0,
      left: 'auto',
      right: 0
    }
  }, {
    props: {
      position: 'sticky'
    },
    style: {
      position: 'sticky',
      zIndex: (theme.vars || theme).zIndex.appBar,
      top: 0,
      left: 'auto',
      right: 0
    }
  }, {
    props: {
      position: 'static'
    },
    style: {
      position: 'static'
    }
  }, {
    props: {
      position: 'relative'
    },
    style: {
      position: 'relative'
    }
  }, {
    props: {
      color: 'inherit'
    },
    style: {
      '--AppBar-color': 'inherit'
    }
  }, {
    props: {
      color: 'default'
    },
    style: {
      '--AppBar-background': theme.vars ? theme.vars.palette.AppBar.defaultBg : theme.palette.grey[100],
      '--AppBar-color': theme.vars ? theme.vars.palette.text.primary : theme.palette.getContrastText(theme.palette.grey[100]),
      ...theme.applyStyles('dark', {
        '--AppBar-background': theme.vars ? theme.vars.palette.AppBar.defaultBg : theme.palette.grey[900],
        '--AppBar-color': theme.vars ? theme.vars.palette.text.primary : theme.palette.getContrastText(theme.palette.grey[900])
      })
    }
  }, ...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)(['contrastText'])).map(([color]) => ({
    props: {
      color
    },
    style: {
      '--AppBar-background': (theme.vars ?? theme).palette[color].main,
      '--AppBar-color': (theme.vars ?? theme).palette[color].contrastText
    }
  })), {
    props: props => props.enableColorOnDark === true && !['inherit', 'transparent'].includes(props.color),
    style: {
      backgroundColor: 'var(--AppBar-background)',
      color: 'var(--AppBar-color)'
    }
  }, {
    props: props => props.enableColorOnDark === false && !['inherit', 'transparent'].includes(props.color),
    style: {
      backgroundColor: 'var(--AppBar-background)',
      color: 'var(--AppBar-color)',
      ...theme.applyStyles('dark', {
        backgroundColor: theme.vars ? joinVars(theme.vars.palette.AppBar.darkBg, 'var(--AppBar-background)') : null,
        color: theme.vars ? joinVars(theme.vars.palette.AppBar.darkColor, 'var(--AppBar-color)') : null
      })
    }
  }, {
    props: {
      color: 'transparent'
    },
    style: {
      '--AppBar-background': 'transparent',
      '--AppBar-color': 'inherit',
      backgroundColor: 'var(--AppBar-background)',
      color: 'var(--AppBar-color)',
      ...theme.applyStyles('dark', {
        backgroundImage: 'none'
      })
    }
  }]
})));
const AppBar = /*#__PURE__*/React.forwardRef(function AppBar(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiAppBar'
  });
  const {
    className,
    color = 'primary',
    enableColorOnDark = false,
    position = 'fixed',
    ...other
  } = props;
  const ownerState = {
    ...props,
    color,
    position,
    enableColorOnDark
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(AppBarRoot, {
    square: true,
    component: "header",
    ownerState: ownerState,
    elevation: 4,
    className: (0, _clsx.default)(classes.root, className, position === 'fixed' && 'mui-fixed'),
    ref: ref,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? AppBar.propTypes /* remove-proptypes */ = {
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
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['default', 'inherit', 'primary', 'secondary', 'transparent', 'error', 'info', 'success', 'warning']), _propTypes.default.string]),
  /**
   * If true, the `color` prop is applied in dark mode.
   * @default false
   */
  enableColorOnDark: _propTypes.default.bool,
  /**
   * The positioning type. The behavior of the different options is described
   * [in the MDN web docs](https://developer.mozilla.org/en-US/docs/Learn/CSS/CSS_layout/Positioning).
   * Note: `sticky` is not universally supported and will fall back to `static` when unavailable.
   * @default 'fixed'
   */
  position: _propTypes.default.oneOf(['absolute', 'fixed', 'relative', 'static', 'sticky']),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = AppBar;