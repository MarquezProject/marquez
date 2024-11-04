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
var _RadioButtonUnchecked = _interopRequireDefault(require("../internal/svg-icons/RadioButtonUnchecked"));
var _RadioButtonChecked = _interopRequireDefault(require("../internal/svg-icons/RadioButtonChecked"));
var _rootShouldForwardProp = _interopRequireDefault(require("../styles/rootShouldForwardProp"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _jsxRuntime = require("react/jsx-runtime");
const RadioButtonIconRoot = (0, _zeroStyled.styled)('span', {
  shouldForwardProp: _rootShouldForwardProp.default
})({
  position: 'relative',
  display: 'flex'
});
const RadioButtonIconBackground = (0, _zeroStyled.styled)(_RadioButtonUnchecked.default)({
  // Scale applied to prevent dot misalignment in Safari
  transform: 'scale(1)'
});
const RadioButtonIconDot = (0, _zeroStyled.styled)(_RadioButtonChecked.default)((0, _memoTheme.default)(({
  theme
}) => ({
  left: 0,
  position: 'absolute',
  transform: 'scale(0)',
  transition: theme.transitions.create('transform', {
    easing: theme.transitions.easing.easeIn,
    duration: theme.transitions.duration.shortest
  }),
  variants: [{
    props: {
      checked: true
    },
    style: {
      transform: 'scale(1)',
      transition: theme.transitions.create('transform', {
        easing: theme.transitions.easing.easeOut,
        duration: theme.transitions.duration.shortest
      })
    }
  }]
})));

/**
 * @ignore - internal component.
 */
function RadioButtonIcon(props) {
  const {
    checked = false,
    classes = {},
    fontSize
  } = props;
  const ownerState = {
    ...props,
    checked
  };
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(RadioButtonIconRoot, {
    className: classes.root,
    ownerState: ownerState,
    children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(RadioButtonIconBackground, {
      fontSize: fontSize,
      className: classes.background,
      ownerState: ownerState
    }), /*#__PURE__*/(0, _jsxRuntime.jsx)(RadioButtonIconDot, {
      fontSize: fontSize,
      className: classes.dot,
      ownerState: ownerState
    })]
  });
}
process.env.NODE_ENV !== "production" ? RadioButtonIcon.propTypes /* remove-proptypes */ = {
  /**
   * If `true`, the component is checked.
   */
  checked: _propTypes.default.bool,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * The size of the component.
   * `small` is equivalent to the dense radio styling.
   */
  fontSize: _propTypes.default.oneOf(['small', 'medium'])
} : void 0;
var _default = exports.default = RadioButtonIcon;