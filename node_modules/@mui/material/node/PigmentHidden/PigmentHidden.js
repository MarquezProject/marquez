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
var _Hidden = _interopRequireDefault(require("@mui/material-pigment-css/Hidden"));
var _capitalize = _interopRequireDefault(require("@mui/utils/capitalize"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _HiddenJs = _interopRequireDefault(require("../Hidden/HiddenJs"));
var _hiddenCssClasses = require("../Hidden/hiddenCssClasses");
var _zeroStyled = require("../zero-styled");
var _jsxRuntime = require("react/jsx-runtime");
// @ts-ignore

const useUtilityClasses = ownerState => {
  const {
    classes,
    breakpoints
  } = ownerState;
  const slots = {
    root: ['root', ...breakpoints.map(({
      breakpoint,
      dir
    }) => {
      return dir === 'only' ? `${dir}${(0, _capitalize.default)(breakpoint)}` : `${breakpoint}${(0, _capitalize.default)(dir)}`;
    })]
  };
  return (0, _composeClasses.default)(slots, _hiddenCssClasses.getHiddenCssUtilityClass, classes);
};
function HiddenCss(props) {
  const theme = (0, _zeroStyled.useTheme)();
  const {
    children,
    className,
    only,
    ...other
  } = props;
  if (process.env.NODE_ENV !== 'production') {
    const unknownProps = Object.keys(other).filter(propName => {
      const isUndeclaredBreakpoint = !theme.breakpoints.keys.some(breakpoint => {
        return `${breakpoint}Up` === propName || `${breakpoint}Down` === propName;
      });
      return !['classes', 'theme', 'isRtl', 'sx'].includes(propName) && isUndeclaredBreakpoint;
    });
    if (unknownProps.length > 0) {
      console.error(`MUI: Unsupported props received by \`<Hidden implementation="css" />\`: ${unknownProps.join(', ')}. Did you forget to wrap this component in a ThemeProvider declaring these breakpoints?`);
    }
  }
  const breakpoints = [];
  for (let i = 0; i < theme.breakpoints.keys.length; i += 1) {
    const breakpoint = theme.breakpoints.keys[i];
    const breakpointUp = other[`${breakpoint}Up`];
    const breakpointDown = other[`${breakpoint}Down`];
    if (breakpointUp) {
      breakpoints.push({
        breakpoint,
        dir: 'up'
      });
    }
    if (breakpointDown) {
      breakpoints.push({
        breakpoint,
        dir: 'down'
      });
    }
  }
  if (only) {
    const onlyBreakpoints = Array.isArray(only) ? only : [only];
    onlyBreakpoints.forEach(breakpoint => {
      breakpoints.push({
        breakpoint,
        dir: 'only'
      });
    });
  }
  const ownerState = {
    ...props,
    classes: {},
    breakpoints
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_Hidden.default, {
    className: (0, _clsx.default)(classes.root, className),
    ...props
  });
}
process.env.NODE_ENV !== "production" ? HiddenCss.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │ To update them, edit the TypeScript types and run `pnpm proptypes`. │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
   */
  children: _propTypes.default.node,
  className: _propTypes.default.string,
  /**
   * Specify which implementation to use.  'js' is the default, 'css' works better for
   * server-side rendering.
   * @default 'js'
   */
  implementation: _propTypes.default.oneOf(['css', 'js']),
  /**
   * You can use this prop when choosing the `js` implementation with server-side rendering.
   *
   * As `window.innerWidth` is unavailable on the server,
   * we default to rendering an empty component during the first mount.
   * You might want to use a heuristic to approximate
   * the screen width of the client browser screen width.
   *
   * For instance, you could be using the user-agent or the client-hints.
   * https://caniuse.com/#search=client%20hint
   */
  initialWidth: _propTypes.default.oneOf(['lg', 'md', 'sm', 'xl', 'xs']),
  /**
   * If `true`, component is hidden on screens below (but not including) this size.
   * @default false
   */
  lgDown: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens this size and above.
   * @default false
   */
  lgUp: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens below (but not including) this size.
   * @default false
   */
  mdDown: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens this size and above.
   * @default false
   */
  mdUp: _propTypes.default.bool,
  /**
   * Hide the given breakpoint(s).
   */
  only: _propTypes.default.oneOfType([_propTypes.default.oneOf(['lg', 'md', 'sm', 'xl', 'xs']), _propTypes.default.arrayOf(_propTypes.default.oneOf(['lg', 'md', 'sm', 'xl', 'xs']).isRequired)]),
  /**
   * If `true`, component is hidden on screens below (but not including) this size.
   * @default false
   */
  smDown: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens this size and above.
   * @default false
   */
  smUp: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens below (but not including) this size.
   * @default false
   */
  xlDown: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens this size and above.
   * @default false
   */
  xlUp: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens below (but not including) this size.
   * @default false
   */
  xsDown: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens this size and above.
   * @default false
   */
  xsUp: _propTypes.default.bool
} : void 0;

/**
 *
 * Demos:
 *
 * - [Hidden](https://mui.com/material-ui/react-hidden/)
 *
 * API:
 *
 * - [PigmentHidden API](https://mui.com/material-ui/api/pigment-hidden/)
 */
function PigmentHidden({
  implementation = 'js',
  ...props
}) {
  if (implementation === 'js') {
    return /*#__PURE__*/(0, _jsxRuntime.jsx)(_HiddenJs.default, {
      ...props
    });
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(HiddenCss, {
    ...props
  });
}
process.env.NODE_ENV !== "production" ? PigmentHidden.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │ To update them, edit the TypeScript types and run `pnpm proptypes`. │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
   */
  children: _propTypes.default.node,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * Specify which implementation to use.  'js' is the default, 'css' works better for
   * server-side rendering.
   * @default 'js'
   */
  implementation: _propTypes.default.oneOf(['css', 'js']),
  /**
   * You can use this prop when choosing the `js` implementation with server-side rendering.
   *
   * As `window.innerWidth` is unavailable on the server,
   * we default to rendering an empty component during the first mount.
   * You might want to use a heuristic to approximate
   * the screen width of the client browser screen width.
   *
   * For instance, you could be using the user-agent or the client-hints.
   * https://caniuse.com/#search=client%20hint
   */
  initialWidth: _propTypes.default.oneOf(['lg', 'md', 'sm', 'xl', 'xs']),
  /**
   * If `true`, component is hidden on screens below (but not including) this size.
   * @default false
   */
  lgDown: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens this size and above.
   * @default false
   */
  lgUp: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens below (but not including) this size.
   * @default false
   */
  mdDown: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens this size and above.
   * @default false
   */
  mdUp: _propTypes.default.bool,
  /**
   * Hide the given breakpoint(s).
   */
  only: _propTypes.default.oneOfType([_propTypes.default.oneOf(['lg', 'md', 'sm', 'xl', 'xs']), _propTypes.default.arrayOf(_propTypes.default.oneOf(['lg', 'md', 'sm', 'xl', 'xs']).isRequired)]),
  /**
   * If `true`, component is hidden on screens below (but not including) this size.
   * @default false
   */
  smDown: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens this size and above.
   * @default false
   */
  smUp: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens below (but not including) this size.
   * @default false
   */
  xlDown: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens this size and above.
   * @default false
   */
  xlUp: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens below (but not including) this size.
   * @default false
   */
  xsDown: _propTypes.default.bool,
  /**
   * If `true`, component is hidden on screens this size and above.
   * @default false
   */
  xsUp: _propTypes.default.bool
} : void 0;
var _default = exports.default = PigmentHidden;