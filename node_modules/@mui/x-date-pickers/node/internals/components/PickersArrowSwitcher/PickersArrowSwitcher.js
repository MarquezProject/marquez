"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.PickersArrowSwitcher = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var _objectWithoutPropertiesLoose2 = _interopRequireDefault(require("@babel/runtime/helpers/objectWithoutPropertiesLoose"));
var React = _interopRequireWildcard(require("react"));
var _clsx = _interopRequireDefault(require("clsx"));
var _Typography = _interopRequireDefault(require("@mui/material/Typography"));
var _RtlProvider = require("@mui/system/RtlProvider");
var _styles = require("@mui/material/styles");
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _useSlotProps3 = _interopRequireDefault(require("@mui/utils/useSlotProps"));
var _IconButton = _interopRequireDefault(require("@mui/material/IconButton"));
var _icons = require("../../../icons");
var _pickersArrowSwitcherClasses = require("./pickersArrowSwitcherClasses");
var _jsxRuntime = require("react/jsx-runtime");
const _excluded = ["children", "className", "slots", "slotProps", "isNextDisabled", "isNextHidden", "onGoToNext", "nextLabel", "isPreviousDisabled", "isPreviousHidden", "onGoToPrevious", "previousLabel", "labelId"],
  _excluded2 = ["ownerState"],
  _excluded3 = ["ownerState"];
const PickersArrowSwitcherRoot = (0, _styles.styled)('div', {
  name: 'MuiPickersArrowSwitcher',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})({
  display: 'flex'
});
const PickersArrowSwitcherSpacer = (0, _styles.styled)('div', {
  name: 'MuiPickersArrowSwitcher',
  slot: 'Spacer',
  overridesResolver: (props, styles) => styles.spacer
})(({
  theme
}) => ({
  width: theme.spacing(3)
}));
const PickersArrowSwitcherButton = (0, _styles.styled)(_IconButton.default, {
  name: 'MuiPickersArrowSwitcher',
  slot: 'Button',
  overridesResolver: (props, styles) => styles.button
})({
  variants: [{
    props: {
      hidden: true
    },
    style: {
      visibility: 'hidden'
    }
  }]
});
const useUtilityClasses = ownerState => {
  const {
    classes
  } = ownerState;
  const slots = {
    root: ['root'],
    spacer: ['spacer'],
    button: ['button'],
    previousIconButton: ['previousIconButton'],
    nextIconButton: ['nextIconButton'],
    leftArrowIcon: ['leftArrowIcon'],
    rightArrowIcon: ['rightArrowIcon']
  };
  return (0, _composeClasses.default)(slots, _pickersArrowSwitcherClasses.getPickersArrowSwitcherUtilityClass, classes);
};
const PickersArrowSwitcher = exports.PickersArrowSwitcher = /*#__PURE__*/React.forwardRef(function PickersArrowSwitcher(inProps, ref) {
  const isRtl = (0, _RtlProvider.useRtl)();
  const props = (0, _styles.useThemeProps)({
    props: inProps,
    name: 'MuiPickersArrowSwitcher'
  });
  const {
      children,
      className,
      slots,
      slotProps,
      isNextDisabled,
      isNextHidden,
      onGoToNext,
      nextLabel,
      isPreviousDisabled,
      isPreviousHidden,
      onGoToPrevious,
      previousLabel,
      labelId
    } = props,
    other = (0, _objectWithoutPropertiesLoose2.default)(props, _excluded);
  const ownerState = props;
  const classes = useUtilityClasses(ownerState);
  const nextProps = {
    isDisabled: isNextDisabled,
    isHidden: isNextHidden,
    goTo: onGoToNext,
    label: nextLabel
  };
  const previousProps = {
    isDisabled: isPreviousDisabled,
    isHidden: isPreviousHidden,
    goTo: onGoToPrevious,
    label: previousLabel
  };
  const PreviousIconButton = slots?.previousIconButton ?? PickersArrowSwitcherButton;
  const previousIconButtonProps = (0, _useSlotProps3.default)({
    elementType: PreviousIconButton,
    externalSlotProps: slotProps?.previousIconButton,
    additionalProps: {
      size: 'medium',
      title: previousProps.label,
      'aria-label': previousProps.label,
      disabled: previousProps.isDisabled,
      edge: 'end',
      onClick: previousProps.goTo
    },
    ownerState: (0, _extends2.default)({}, ownerState, {
      hidden: previousProps.isHidden
    }),
    className: (0, _clsx.default)(classes.button, classes.previousIconButton)
  });
  const NextIconButton = slots?.nextIconButton ?? PickersArrowSwitcherButton;
  const nextIconButtonProps = (0, _useSlotProps3.default)({
    elementType: NextIconButton,
    externalSlotProps: slotProps?.nextIconButton,
    additionalProps: {
      size: 'medium',
      title: nextProps.label,
      'aria-label': nextProps.label,
      disabled: nextProps.isDisabled,
      edge: 'start',
      onClick: nextProps.goTo
    },
    ownerState: (0, _extends2.default)({}, ownerState, {
      hidden: nextProps.isHidden
    }),
    className: (0, _clsx.default)(classes.button, classes.nextIconButton)
  });
  const LeftArrowIcon = slots?.leftArrowIcon ?? _icons.ArrowLeftIcon;
  // The spread is here to avoid this bug mui/material-ui#34056
  const _useSlotProps = (0, _useSlotProps3.default)({
      elementType: LeftArrowIcon,
      externalSlotProps: slotProps?.leftArrowIcon,
      additionalProps: {
        fontSize: 'inherit'
      },
      ownerState,
      className: classes.leftArrowIcon
    }),
    leftArrowIconProps = (0, _objectWithoutPropertiesLoose2.default)(_useSlotProps, _excluded2);
  const RightArrowIcon = slots?.rightArrowIcon ?? _icons.ArrowRightIcon;
  // The spread is here to avoid this bug mui/material-ui#34056
  const _useSlotProps2 = (0, _useSlotProps3.default)({
      elementType: RightArrowIcon,
      externalSlotProps: slotProps?.rightArrowIcon,
      additionalProps: {
        fontSize: 'inherit'
      },
      ownerState,
      className: classes.rightArrowIcon
    }),
    rightArrowIconProps = (0, _objectWithoutPropertiesLoose2.default)(_useSlotProps2, _excluded3);
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(PickersArrowSwitcherRoot, (0, _extends2.default)({
    ref: ref,
    className: (0, _clsx.default)(classes.root, className),
    ownerState: ownerState
  }, other, {
    children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(PreviousIconButton, (0, _extends2.default)({}, previousIconButtonProps, {
      children: isRtl ? /*#__PURE__*/(0, _jsxRuntime.jsx)(RightArrowIcon, (0, _extends2.default)({}, rightArrowIconProps)) : /*#__PURE__*/(0, _jsxRuntime.jsx)(LeftArrowIcon, (0, _extends2.default)({}, leftArrowIconProps))
    })), children ? /*#__PURE__*/(0, _jsxRuntime.jsx)(_Typography.default, {
      variant: "subtitle1",
      component: "span",
      id: labelId,
      children: children
    }) : /*#__PURE__*/(0, _jsxRuntime.jsx)(PickersArrowSwitcherSpacer, {
      className: classes.spacer,
      ownerState: ownerState
    }), /*#__PURE__*/(0, _jsxRuntime.jsx)(NextIconButton, (0, _extends2.default)({}, nextIconButtonProps, {
      children: isRtl ? /*#__PURE__*/(0, _jsxRuntime.jsx)(LeftArrowIcon, (0, _extends2.default)({}, leftArrowIconProps)) : /*#__PURE__*/(0, _jsxRuntime.jsx)(RightArrowIcon, (0, _extends2.default)({}, rightArrowIconProps))
    }))]
  }));
});