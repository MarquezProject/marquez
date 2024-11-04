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
var _ButtonBase = _interopRequireDefault(require("../ButtonBase"));
var _unsupportedProp = _interopRequireDefault(require("../utils/unsupportedProp"));
var _bottomNavigationActionClasses = _interopRequireWildcard(require("./bottomNavigationActionClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    showLabel,
    selected
  } = ownerState;
  const slots = {
    root: ['root', !showLabel && !selected && 'iconOnly', selected && 'selected'],
    label: ['label', !showLabel && !selected && 'iconOnly', selected && 'selected']
  };
  return (0, _composeClasses.default)(slots, _bottomNavigationActionClasses.getBottomNavigationActionUtilityClass, classes);
};
const BottomNavigationActionRoot = (0, _zeroStyled.styled)(_ButtonBase.default, {
  name: 'MuiBottomNavigationAction',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, !ownerState.showLabel && !ownerState.selected && styles.iconOnly];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  transition: theme.transitions.create(['color', 'padding-top'], {
    duration: theme.transitions.duration.short
  }),
  padding: '0px 12px',
  minWidth: 80,
  maxWidth: 168,
  color: (theme.vars || theme).palette.text.secondary,
  flexDirection: 'column',
  flex: '1',
  [`&.${_bottomNavigationActionClasses.default.selected}`]: {
    color: (theme.vars || theme).palette.primary.main
  },
  variants: [{
    props: ({
      showLabel,
      selected
    }) => !showLabel && !selected,
    style: {
      paddingTop: 14
    }
  }, {
    props: ({
      showLabel,
      selected,
      label
    }) => !showLabel && !selected && !label,
    style: {
      paddingTop: 0
    }
  }]
})));
const BottomNavigationActionLabel = (0, _zeroStyled.styled)('span', {
  name: 'MuiBottomNavigationAction',
  slot: 'Label',
  overridesResolver: (props, styles) => styles.label
})((0, _memoTheme.default)(({
  theme
}) => ({
  fontFamily: theme.typography.fontFamily,
  fontSize: theme.typography.pxToRem(12),
  opacity: 1,
  transition: 'font-size 0.2s, opacity 0.2s',
  transitionDelay: '0.1s',
  [`&.${_bottomNavigationActionClasses.default.selected}`]: {
    fontSize: theme.typography.pxToRem(14)
  },
  variants: [{
    props: ({
      showLabel,
      selected
    }) => !showLabel && !selected,
    style: {
      opacity: 0,
      transitionDelay: '0s'
    }
  }]
})));
const BottomNavigationAction = /*#__PURE__*/React.forwardRef(function BottomNavigationAction(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiBottomNavigationAction'
  });
  const {
    className,
    icon,
    label,
    onChange,
    onClick,
    // eslint-disable-next-line react/prop-types -- private, always overridden by BottomNavigation
    selected,
    showLabel,
    value,
    ...other
  } = props;
  const ownerState = props;
  const classes = useUtilityClasses(ownerState);
  const handleChange = event => {
    if (onChange) {
      onChange(event, value);
    }
    if (onClick) {
      onClick(event);
    }
  };
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(BottomNavigationActionRoot, {
    ref: ref,
    className: (0, _clsx.default)(classes.root, className),
    focusRipple: true,
    onClick: handleChange,
    ownerState: ownerState,
    ...other,
    children: [icon, /*#__PURE__*/(0, _jsxRuntime.jsx)(BottomNavigationActionLabel, {
      className: classes.label,
      ownerState: ownerState,
      children: label
    })]
  });
});
process.env.NODE_ENV !== "production" ? BottomNavigationAction.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * This prop isn't supported.
   * Use the `component` prop if you need to change the children structure.
   */
  children: _unsupportedProp.default,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The icon to display.
   */
  icon: _propTypes.default.node,
  /**
   * The label element.
   */
  label: _propTypes.default.node,
  /**
   * @ignore
   */
  onChange: _propTypes.default.func,
  /**
   * @ignore
   */
  onClick: _propTypes.default.func,
  /**
   * If `true`, the `BottomNavigationAction` will show its label.
   * By default, only the selected `BottomNavigationAction`
   * inside `BottomNavigation` will show its label.
   *
   * The prop defaults to the value (`false`) inherited from the parent BottomNavigation component.
   */
  showLabel: _propTypes.default.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * You can provide your own value. Otherwise, we fallback to the child position index.
   */
  value: _propTypes.default.any
} : void 0;
var _default = exports.default = BottomNavigationAction;