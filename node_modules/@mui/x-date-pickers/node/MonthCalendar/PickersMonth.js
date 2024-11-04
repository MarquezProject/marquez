"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.PickersMonth = void 0;
var _objectWithoutPropertiesLoose2 = _interopRequireDefault(require("@babel/runtime/helpers/objectWithoutPropertiesLoose"));
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var React = _interopRequireWildcard(require("react"));
var _clsx = _interopRequireDefault(require("clsx"));
var _styles = require("@mui/material/styles");
var _useSlotProps = _interopRequireDefault(require("@mui/utils/useSlotProps"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _useEnhancedEffect = _interopRequireDefault(require("@mui/utils/useEnhancedEffect"));
var _pickersMonthClasses = require("./pickersMonthClasses");
var _jsxRuntime = require("react/jsx-runtime");
const _excluded = ["autoFocus", "className", "children", "disabled", "selected", "value", "tabIndex", "onClick", "onKeyDown", "onFocus", "onBlur", "aria-current", "aria-label", "monthsPerRow", "slots", "slotProps"];
const useUtilityClasses = ownerState => {
  const {
    disabled,
    selected,
    classes
  } = ownerState;
  const slots = {
    root: ['root'],
    monthButton: ['monthButton', disabled && 'disabled', selected && 'selected']
  };
  return (0, _composeClasses.default)(slots, _pickersMonthClasses.getPickersMonthUtilityClass, classes);
};
const PickersMonthRoot = (0, _styles.styled)('div', {
  name: 'MuiPickersMonth',
  slot: 'Root',
  overridesResolver: (_, styles) => [styles.root]
})({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  flexBasis: '33.3%',
  variants: [{
    props: {
      monthsPerRow: 4
    },
    style: {
      flexBasis: '25%'
    }
  }]
});
const MonthCalendarButton = (0, _styles.styled)('button', {
  name: 'MuiPickersMonth',
  slot: 'MonthButton',
  overridesResolver: (_, styles) => [styles.monthButton, {
    [`&.${_pickersMonthClasses.pickersMonthClasses.disabled}`]: styles.disabled
  }, {
    [`&.${_pickersMonthClasses.pickersMonthClasses.selected}`]: styles.selected
  }]
})(({
  theme
}) => (0, _extends2.default)({
  color: 'unset',
  backgroundColor: 'transparent',
  border: 0,
  outline: 0
}, theme.typography.subtitle1, {
  margin: '8px 0',
  height: 36,
  width: 72,
  borderRadius: 18,
  cursor: 'pointer',
  '&:focus': {
    backgroundColor: theme.vars ? `rgba(${theme.vars.palette.action.activeChannel} / ${theme.vars.palette.action.hoverOpacity})` : (0, _styles.alpha)(theme.palette.action.active, theme.palette.action.hoverOpacity)
  },
  '&:hover': {
    backgroundColor: theme.vars ? `rgba(${theme.vars.palette.action.activeChannel} / ${theme.vars.palette.action.hoverOpacity})` : (0, _styles.alpha)(theme.palette.action.active, theme.palette.action.hoverOpacity)
  },
  '&:disabled': {
    cursor: 'auto',
    pointerEvents: 'none'
  },
  [`&.${_pickersMonthClasses.pickersMonthClasses.disabled}`]: {
    color: (theme.vars || theme).palette.text.secondary
  },
  [`&.${_pickersMonthClasses.pickersMonthClasses.selected}`]: {
    color: (theme.vars || theme).palette.primary.contrastText,
    backgroundColor: (theme.vars || theme).palette.primary.main,
    '&:focus, &:hover': {
      backgroundColor: (theme.vars || theme).palette.primary.dark
    }
  }
}));

/**
 * @ignore - do not document.
 */
const PickersMonth = exports.PickersMonth = /*#__PURE__*/React.memo(function PickersMonth(inProps) {
  const props = (0, _styles.useThemeProps)({
    props: inProps,
    name: 'MuiPickersMonth'
  });
  const {
      autoFocus,
      className,
      children,
      disabled,
      selected,
      value,
      tabIndex,
      onClick,
      onKeyDown,
      onFocus,
      onBlur,
      'aria-current': ariaCurrent,
      'aria-label': ariaLabel
      // We don't want to forward this prop to the root element
      ,

      slots,
      slotProps
    } = props,
    other = (0, _objectWithoutPropertiesLoose2.default)(props, _excluded);
  const ref = React.useRef(null);
  const classes = useUtilityClasses(props);

  // We can't forward the `autoFocus` to the button because it is a native button, not a MUI Button
  (0, _useEnhancedEffect.default)(() => {
    if (autoFocus) {
      // `ref.current` being `null` would be a bug in MUI.
      ref.current?.focus();
    }
  }, [autoFocus]);
  const MonthButton = slots?.monthButton ?? MonthCalendarButton;
  const monthButtonProps = (0, _useSlotProps.default)({
    elementType: MonthButton,
    externalSlotProps: slotProps?.monthButton,
    additionalProps: {
      children,
      disabled,
      tabIndex,
      ref,
      type: 'button',
      role: 'radio',
      'aria-current': ariaCurrent,
      'aria-checked': selected,
      'aria-label': ariaLabel,
      onClick: event => onClick(event, value),
      onKeyDown: event => onKeyDown(event, value),
      onFocus: event => onFocus(event, value),
      onBlur: event => onBlur(event, value)
    },
    ownerState: props,
    className: classes.monthButton
  });
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(PickersMonthRoot, (0, _extends2.default)({
    className: (0, _clsx.default)(classes.root, className),
    ownerState: props
  }, other, {
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(MonthButton, (0, _extends2.default)({}, monthButtonProps))
  }));
});