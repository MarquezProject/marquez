"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _Container = _interopRequireDefault(require("@mui/material-pigment-css/Container"));
var _capitalize = _interopRequireDefault(require("@mui/utils/capitalize"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _generateUtilityClass = _interopRequireDefault(require("@mui/utils/generateUtilityClass"));
var _jsxRuntime = require("react/jsx-runtime");
// @ts-ignore

const useUtilityClasses = ownerState => {
  const {
    classes,
    fixed,
    disableGutters,
    maxWidth
  } = ownerState;
  const slots = {
    root: ['root', maxWidth && `maxWidth${(0, _capitalize.default)(String(maxWidth))}`, fixed && 'fixed', disableGutters && 'disableGutters']
  };
  return (0, _composeClasses.default)(slots, slot => (0, _generateUtilityClass.default)('MuiContainer', slot), classes);
};
/**
 *
 * Demos:
 *
 * - [Container](https://mui.com/material-ui/react-container/)
 *
 * API:
 *
 * - [PigmentContainer API](https://mui.com/material-ui/api/pigment-container/)
 */
const PigmentContainer = /*#__PURE__*/React.forwardRef(function PigmentContainer({
  className,
  disableGutters = false,
  fixed = false,
  maxWidth = 'lg',
  ...props
}, ref) {
  const ownerState = {
    ...props,
    disableGutters,
    fixed,
    maxWidth
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_Container.default, {
    className: (0, _clsx.default)(classes.root, className),
    disableGutters: disableGutters,
    fixed: fixed,
    maxWidth: maxWidth,
    ...props,
    // @ts-ignore
    ref: ref
  });
});
process.env.NODE_ENV !== "production" ? PigmentContainer.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │ To update them, edit the TypeScript types and run `pnpm proptypes`. │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * @ignore
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
   * If `true`, the left and right padding is removed.
   * @default false
   */
  disableGutters: _propTypes.default.bool,
  /**
   * Set the max-width to match the min-width of the current breakpoint.
   * This is useful if you'd prefer to design for a fixed set of sizes
   * instead of trying to accommodate a fully fluid viewport.
   * It's fluid by default.
   * @default false
   */
  fixed: _propTypes.default.bool,
  /**
   * Determine the max-width of the container.
   * The container width grows with the size of the screen.
   * Set to `false` to disable `maxWidth`.
   * @default 'lg'
   */
  maxWidth: _propTypes.default.oneOf(['lg', 'md', 'sm', 'xl', 'xs', false]),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = PigmentContainer;