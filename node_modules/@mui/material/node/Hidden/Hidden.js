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
var _HiddenJs = _interopRequireDefault(require("./HiddenJs"));
var _HiddenCss = _interopRequireDefault(require("./HiddenCss"));
var _jsxRuntime = require("react/jsx-runtime");
/**
 * Responsively hides children based on the selected implementation.
 *
 * @deprecated The Hidden component was deprecated in Material UI v5. To learn more, see [the Hidden section](https://mui.com/material-ui/migration/v5-component-changes/#hidden) of the migration docs.
 */function Hidden(props) {
  const {
    implementation = 'js',
    lgDown = false,
    lgUp = false,
    mdDown = false,
    mdUp = false,
    smDown = false,
    smUp = false,
    xlDown = false,
    xlUp = false,
    xsDown = false,
    xsUp = false,
    ...other
  } = props;
  if (implementation === 'js') {
    return /*#__PURE__*/(0, _jsxRuntime.jsx)(_HiddenJs.default, {
      lgDown: lgDown,
      lgUp: lgUp,
      mdDown: mdDown,
      mdUp: mdUp,
      smDown: smDown,
      smUp: smUp,
      xlDown: xlDown,
      xlUp: xlUp,
      xsDown: xsDown,
      xsUp: xsUp,
      ...other
    });
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_HiddenCss.default, {
    lgDown: lgDown,
    lgUp: lgUp,
    mdDown: mdDown,
    mdUp: mdUp,
    smDown: smDown,
    smUp: smUp,
    xlDown: xlDown,
    xlUp: xlUp,
    xsDown: xsDown,
    xsUp: xsUp,
    ...other
  });
}
process.env.NODE_ENV !== "production" ? Hidden.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
   */
  children: _propTypes.default.node,
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
  initialWidth: _propTypes.default.oneOf(['xs', 'sm', 'md', 'lg', 'xl']),
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
  only: _propTypes.default.oneOfType([_propTypes.default.oneOf(['xs', 'sm', 'md', 'lg', 'xl']), _propTypes.default.arrayOf(_propTypes.default.oneOf(['xs', 'sm', 'md', 'lg', 'xl']).isRequired)]),
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
var _default = exports.default = Hidden;