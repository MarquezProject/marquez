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
var _jsxRuntime = require("react/jsx-runtime");
/**
 * @ignore - internal component.
 */function Ripple(props) {
  const {
    className,
    classes,
    pulsate = false,
    rippleX,
    rippleY,
    rippleSize,
    in: inProp,
    onExited,
    timeout
  } = props;
  const [leaving, setLeaving] = React.useState(false);
  const rippleClassName = (0, _clsx.default)(className, classes.ripple, classes.rippleVisible, pulsate && classes.ripplePulsate);
  const rippleStyles = {
    width: rippleSize,
    height: rippleSize,
    top: -(rippleSize / 2) + rippleY,
    left: -(rippleSize / 2) + rippleX
  };
  const childClassName = (0, _clsx.default)(classes.child, leaving && classes.childLeaving, pulsate && classes.childPulsate);
  if (!inProp && !leaving) {
    setLeaving(true);
  }
  React.useEffect(() => {
    if (!inProp && onExited != null) {
      // react-transition-group#onExited
      const timeoutId = setTimeout(onExited, timeout);
      return () => {
        clearTimeout(timeoutId);
      };
    }
    return undefined;
  }, [onExited, inProp, timeout]);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)("span", {
    className: rippleClassName,
    style: rippleStyles,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)("span", {
      className: childClassName
    })
  });
}
process.env.NODE_ENV !== "production" ? Ripple.propTypes /* remove-proptypes */ = {
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object.isRequired,
  className: _propTypes.default.string,
  /**
   * @ignore - injected from TransitionGroup
   */
  in: _propTypes.default.bool,
  /**
   * @ignore - injected from TransitionGroup
   */
  onExited: _propTypes.default.func,
  /**
   * If `true`, the ripple pulsates, typically indicating the keyboard focus state of an element.
   */
  pulsate: _propTypes.default.bool,
  /**
   * Diameter of the ripple.
   */
  rippleSize: _propTypes.default.number,
  /**
   * Horizontal position of the ripple center.
   */
  rippleX: _propTypes.default.number,
  /**
   * Vertical position of the ripple center.
   */
  rippleY: _propTypes.default.number,
  /**
   * exit delay
   */
  timeout: _propTypes.default.number.isRequired
} : void 0;
var _default = exports.default = Ripple;