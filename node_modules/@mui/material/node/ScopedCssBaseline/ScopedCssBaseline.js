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
var _CssBaseline = require("../CssBaseline/CssBaseline");
var _scopedCssBaselineClasses = require("./scopedCssBaselineClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes
  } = ownerState;
  const slots = {
    root: ['root']
  };
  return (0, _composeClasses.default)(slots, _scopedCssBaselineClasses.getScopedCssBaselineUtilityClass, classes);
};
const ScopedCssBaselineRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiScopedCssBaseline',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})((0, _memoTheme.default)(({
  theme
}) => {
  const colorSchemeStyles = {};
  if (theme.colorSchemes) {
    Object.entries(theme.colorSchemes).forEach(([key, scheme]) => {
      const selector = theme.getColorSchemeSelector(key);
      if (selector.startsWith('@')) {
        colorSchemeStyles[selector] = {
          colorScheme: scheme.palette?.mode
        };
      } else {
        colorSchemeStyles[`&${selector.replace(/\s*&/, '')}`] = {
          colorScheme: scheme.palette?.mode
        };
      }
    });
  }
  return {
    ...(0, _CssBaseline.html)(theme, false),
    ...(0, _CssBaseline.body)(theme),
    '& *, & *::before, & *::after': {
      boxSizing: 'inherit'
    },
    '& strong, & b': {
      fontWeight: theme.typography.fontWeightBold
    },
    variants: [{
      props: {
        enableColorScheme: true
      },
      style: theme.vars ? colorSchemeStyles : {
        colorScheme: theme.palette.mode
      }
    }]
  };
}));
const ScopedCssBaseline = /*#__PURE__*/React.forwardRef(function ScopedCssBaseline(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiScopedCssBaseline'
  });
  const {
    className,
    component = 'div',
    enableColorScheme,
    ...other
  } = props;
  const ownerState = {
    ...props,
    component
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(ScopedCssBaselineRoot, {
    as: component,
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? ScopedCssBaseline.propTypes /* remove-proptypes */ = {
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
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * Enable `color-scheme` CSS property to use `theme.palette.mode`.
   * For more details, check out https://developer.mozilla.org/en-US/docs/Web/CSS/color-scheme
   * For browser support, check out https://caniuse.com/?search=color-scheme
   */
  enableColorScheme: _propTypes.default.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = ScopedCssBaseline;