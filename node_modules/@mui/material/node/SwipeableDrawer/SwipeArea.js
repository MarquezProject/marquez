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
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _rootShouldForwardProp = _interopRequireDefault(require("../styles/rootShouldForwardProp"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _Drawer = require("../Drawer/Drawer");
var _jsxRuntime = require("react/jsx-runtime");
const SwipeAreaRoot = (0, _zeroStyled.styled)('div', {
  shouldForwardProp: _rootShouldForwardProp.default
})((0, _memoTheme.default)(({
  theme
}) => ({
  position: 'fixed',
  top: 0,
  left: 0,
  bottom: 0,
  zIndex: theme.zIndex.drawer - 1,
  variants: [{
    props: {
      anchor: 'left'
    },
    style: {
      right: 'auto'
    }
  }, {
    props: {
      anchor: 'right'
    },
    style: {
      left: 'auto',
      right: 0
    }
  }, {
    props: {
      anchor: 'top'
    },
    style: {
      bottom: 'auto',
      right: 0
    }
  }, {
    props: {
      anchor: 'bottom'
    },
    style: {
      top: 'auto',
      bottom: 0,
      right: 0
    }
  }]
})));

/**
 * @ignore - internal component.
 */
const SwipeArea = /*#__PURE__*/React.forwardRef(function SwipeArea(props, ref) {
  const {
    anchor,
    classes = {},
    className,
    width,
    style,
    ...other
  } = props;
  const ownerState = props;
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(SwipeAreaRoot, {
    className: (0, _clsx.default)('PrivateSwipeArea-root', classes.root, classes[`anchor${(0, _capitalize.default)(anchor)}`], className),
    ref: ref,
    style: {
      [(0, _Drawer.isHorizontal)(anchor) ? 'width' : 'height']: width,
      ...style
    },
    ownerState: ownerState,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? SwipeArea.propTypes = {
  /**
   * Side on which to attach the discovery area.
   */
  anchor: _propTypes.default.oneOf(['left', 'top', 'right', 'bottom']).isRequired,
  /**
   * @ignore
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * @ignore
   */
  style: _propTypes.default.object,
  /**
   * The width of the left most (or right most) area in `px` where the
   * drawer can be swiped open from.
   */
  width: _propTypes.default.number.isRequired
} : void 0;
var _default = exports.default = SwipeArea;