"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.PickersToolbarText = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var _objectWithoutPropertiesLoose2 = _interopRequireDefault(require("@babel/runtime/helpers/objectWithoutPropertiesLoose"));
var React = _interopRequireWildcard(require("react"));
var _clsx = _interopRequireDefault(require("clsx"));
var _Typography = _interopRequireDefault(require("@mui/material/Typography"));
var _styles = require("@mui/material/styles");
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _pickersToolbarTextClasses = require("./pickersToolbarTextClasses");
var _jsxRuntime = require("react/jsx-runtime");
const _excluded = ["className", "selected", "value"];
const useUtilityClasses = ownerState => {
  const {
    classes,
    selected
  } = ownerState;
  const slots = {
    root: ['root', selected && 'selected']
  };
  return (0, _composeClasses.default)(slots, _pickersToolbarTextClasses.getPickersToolbarTextUtilityClass, classes);
};
const PickersToolbarTextRoot = (0, _styles.styled)(_Typography.default, {
  name: 'MuiPickersToolbarText',
  slot: 'Root',
  overridesResolver: (_, styles) => [styles.root, {
    [`&.${_pickersToolbarTextClasses.pickersToolbarTextClasses.selected}`]: styles.selected
  }]
})(({
  theme
}) => ({
  transition: theme.transitions.create('color'),
  color: (theme.vars || theme).palette.text.secondary,
  [`&.${_pickersToolbarTextClasses.pickersToolbarTextClasses.selected}`]: {
    color: (theme.vars || theme).palette.text.primary
  }
}));
const PickersToolbarText = exports.PickersToolbarText = /*#__PURE__*/React.forwardRef(function PickersToolbarText(inProps, ref) {
  const props = (0, _styles.useThemeProps)({
    props: inProps,
    name: 'MuiPickersToolbarText'
  });
  const {
      className,
      value
    } = props,
    other = (0, _objectWithoutPropertiesLoose2.default)(props, _excluded);
  const classes = useUtilityClasses(props);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(PickersToolbarTextRoot, (0, _extends2.default)({
    ref: ref,
    className: (0, _clsx.default)(classes.root, className),
    component: "span"
  }, other, {
    children: value
  }));
});