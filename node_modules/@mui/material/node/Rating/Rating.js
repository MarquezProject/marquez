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
var _clamp = _interopRequireDefault(require("@mui/utils/clamp"));
var _visuallyHidden = _interopRequireDefault(require("@mui/utils/visuallyHidden"));
var _chainPropTypes = _interopRequireDefault(require("@mui/utils/chainPropTypes"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _RtlProvider = require("@mui/system/RtlProvider");
var _isFocusVisible = _interopRequireDefault(require("@mui/utils/isFocusVisible"));
var _utils = require("../utils");
var _Star = _interopRequireDefault(require("../internal/svg-icons/Star"));
var _StarBorder = _interopRequireDefault(require("../internal/svg-icons/StarBorder"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _slotShouldForwardProp = _interopRequireDefault(require("../styles/slotShouldForwardProp"));
var _ratingClasses = _interopRequireWildcard(require("./ratingClasses"));
var _jsxRuntime = require("react/jsx-runtime");
function getDecimalPrecision(num) {
  const decimalPart = num.toString().split('.')[1];
  return decimalPart ? decimalPart.length : 0;
}
function roundValueToPrecision(value, precision) {
  if (value == null) {
    return value;
  }
  const nearest = Math.round(value / precision) * precision;
  return Number(nearest.toFixed(getDecimalPrecision(precision)));
}
const useUtilityClasses = ownerState => {
  const {
    classes,
    size,
    readOnly,
    disabled,
    emptyValueFocused,
    focusVisible
  } = ownerState;
  const slots = {
    root: ['root', `size${(0, _utils.capitalize)(size)}`, disabled && 'disabled', focusVisible && 'focusVisible', readOnly && 'readOnly'],
    label: ['label', 'pristine'],
    labelEmptyValue: [emptyValueFocused && 'labelEmptyValueActive'],
    icon: ['icon'],
    iconEmpty: ['iconEmpty'],
    iconFilled: ['iconFilled'],
    iconHover: ['iconHover'],
    iconFocus: ['iconFocus'],
    iconActive: ['iconActive'],
    decimal: ['decimal'],
    visuallyHidden: ['visuallyHidden']
  };
  return (0, _composeClasses.default)(slots, _ratingClasses.getRatingUtilityClass, classes);
};
const RatingRoot = (0, _zeroStyled.styled)('span', {
  name: 'MuiRating',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [{
      [`& .${_ratingClasses.default.visuallyHidden}`]: styles.visuallyHidden
    }, styles.root, styles[`size${(0, _utils.capitalize)(ownerState.size)}`], ownerState.readOnly && styles.readOnly];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'inline-flex',
  // Required to position the pristine input absolutely
  position: 'relative',
  fontSize: theme.typography.pxToRem(24),
  color: '#faaf00',
  cursor: 'pointer',
  textAlign: 'left',
  width: 'min-content',
  WebkitTapHighlightColor: 'transparent',
  [`&.${_ratingClasses.default.disabled}`]: {
    opacity: (theme.vars || theme).palette.action.disabledOpacity,
    pointerEvents: 'none'
  },
  [`&.${_ratingClasses.default.focusVisible} .${_ratingClasses.default.iconActive}`]: {
    outline: '1px solid #999'
  },
  [`& .${_ratingClasses.default.visuallyHidden}`]: _visuallyHidden.default,
  variants: [{
    props: {
      size: 'small'
    },
    style: {
      fontSize: theme.typography.pxToRem(18)
    }
  }, {
    props: {
      size: 'large'
    },
    style: {
      fontSize: theme.typography.pxToRem(30)
    }
  }, {
    // TODO v6: use the .Mui-readOnly global state class
    props: ({
      ownerState
    }) => ownerState.readOnly,
    style: {
      pointerEvents: 'none'
    }
  }]
})));
const RatingLabel = (0, _zeroStyled.styled)('label', {
  name: 'MuiRating',
  slot: 'Label',
  overridesResolver: ({
    ownerState
  }, styles) => [styles.label, ownerState.emptyValueFocused && styles.labelEmptyValueActive]
})({
  cursor: 'inherit',
  variants: [{
    props: ({
      ownerState
    }) => ownerState.emptyValueFocused,
    style: {
      top: 0,
      bottom: 0,
      position: 'absolute',
      outline: '1px solid #999',
      width: '100%'
    }
  }]
});
const RatingIcon = (0, _zeroStyled.styled)('span', {
  name: 'MuiRating',
  slot: 'Icon',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.icon, ownerState.iconEmpty && styles.iconEmpty, ownerState.iconFilled && styles.iconFilled, ownerState.iconHover && styles.iconHover, ownerState.iconFocus && styles.iconFocus, ownerState.iconActive && styles.iconActive];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  // Fit wrapper to actual icon size.
  display: 'flex',
  transition: theme.transitions.create('transform', {
    duration: theme.transitions.duration.shortest
  }),
  // Fix mouseLeave issue.
  // https://github.com/facebook/react/issues/4492
  pointerEvents: 'none',
  variants: [{
    props: ({
      ownerState
    }) => ownerState.iconActive,
    style: {
      transform: 'scale(1.2)'
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.iconEmpty,
    style: {
      color: (theme.vars || theme).palette.action.disabled
    }
  }]
})));
const RatingDecimal = (0, _zeroStyled.styled)('span', {
  name: 'MuiRating',
  slot: 'Decimal',
  shouldForwardProp: prop => (0, _slotShouldForwardProp.default)(prop) && prop !== 'iconActive',
  overridesResolver: (props, styles) => {
    const {
      iconActive
    } = props;
    return [styles.decimal, iconActive && styles.iconActive];
  }
})({
  position: 'relative',
  variants: [{
    props: ({
      iconActive
    }) => iconActive,
    style: {
      transform: 'scale(1.2)'
    }
  }]
});
function IconContainer(props) {
  const {
    value,
    ...other
  } = props;
  return /*#__PURE__*/(0, _jsxRuntime.jsx)("span", {
    ...other
  });
}
process.env.NODE_ENV !== "production" ? IconContainer.propTypes = {
  value: _propTypes.default.number.isRequired
} : void 0;
function RatingItem(props) {
  const {
    classes,
    disabled,
    emptyIcon,
    focus,
    getLabelText,
    highlightSelectedOnly,
    hover,
    icon,
    IconContainerComponent,
    isActive,
    itemValue,
    labelProps,
    name,
    onBlur,
    onChange,
    onClick,
    onFocus,
    readOnly,
    ownerState,
    ratingValue,
    ratingValueRounded
  } = props;
  const isFilled = highlightSelectedOnly ? itemValue === ratingValue : itemValue <= ratingValue;
  const isHovered = itemValue <= hover;
  const isFocused = itemValue <= focus;
  const isChecked = itemValue === ratingValueRounded;

  // "name" ensures unique IDs across different Rating components in React 17,
  // preventing one component from affecting another. React 18's useId already handles this.
  // Update to const id = useId(); when React 17 support is dropped.
  // More details: https://github.com/mui/material-ui/issues/40997
  const id = `${name}-${(0, _utils.unstable_useId)()}`;
  const container = /*#__PURE__*/(0, _jsxRuntime.jsx)(RatingIcon, {
    as: IconContainerComponent,
    value: itemValue,
    className: (0, _clsx.default)(classes.icon, isFilled ? classes.iconFilled : classes.iconEmpty, isHovered && classes.iconHover, isFocused && classes.iconFocus, isActive && classes.iconActive),
    ownerState: {
      ...ownerState,
      iconEmpty: !isFilled,
      iconFilled: isFilled,
      iconHover: isHovered,
      iconFocus: isFocused,
      iconActive: isActive
    },
    children: emptyIcon && !isFilled ? emptyIcon : icon
  });
  if (readOnly) {
    return /*#__PURE__*/(0, _jsxRuntime.jsx)("span", {
      ...labelProps,
      children: container
    });
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(React.Fragment, {
    children: [/*#__PURE__*/(0, _jsxRuntime.jsxs)(RatingLabel, {
      ownerState: {
        ...ownerState,
        emptyValueFocused: undefined
      },
      htmlFor: id,
      ...labelProps,
      children: [container, /*#__PURE__*/(0, _jsxRuntime.jsx)("span", {
        className: classes.visuallyHidden,
        children: getLabelText(itemValue)
      })]
    }), /*#__PURE__*/(0, _jsxRuntime.jsx)("input", {
      className: classes.visuallyHidden,
      onFocus: onFocus,
      onBlur: onBlur,
      onChange: onChange,
      onClick: onClick,
      disabled: disabled,
      value: itemValue,
      id: id,
      type: "radio",
      name: name,
      checked: isChecked
    })]
  });
}
process.env.NODE_ENV !== "production" ? RatingItem.propTypes = {
  classes: _propTypes.default.object.isRequired,
  disabled: _propTypes.default.bool.isRequired,
  emptyIcon: _propTypes.default.node,
  focus: _propTypes.default.number.isRequired,
  getLabelText: _propTypes.default.func.isRequired,
  highlightSelectedOnly: _propTypes.default.bool.isRequired,
  hover: _propTypes.default.number.isRequired,
  icon: _propTypes.default.node,
  IconContainerComponent: _propTypes.default.elementType.isRequired,
  isActive: _propTypes.default.bool.isRequired,
  itemValue: _propTypes.default.number.isRequired,
  labelProps: _propTypes.default.object,
  name: _propTypes.default.string,
  onBlur: _propTypes.default.func.isRequired,
  onChange: _propTypes.default.func.isRequired,
  onClick: _propTypes.default.func.isRequired,
  onFocus: _propTypes.default.func.isRequired,
  ownerState: _propTypes.default.object.isRequired,
  ratingValue: _propTypes.default.number,
  ratingValueRounded: _propTypes.default.number,
  readOnly: _propTypes.default.bool.isRequired
} : void 0;
const defaultIcon = /*#__PURE__*/(0, _jsxRuntime.jsx)(_Star.default, {
  fontSize: "inherit"
});
const defaultEmptyIcon = /*#__PURE__*/(0, _jsxRuntime.jsx)(_StarBorder.default, {
  fontSize: "inherit"
});
function defaultLabelText(value) {
  return `${value || '0'} Star${value !== 1 ? 's' : ''}`;
}
const Rating = /*#__PURE__*/React.forwardRef(function Rating(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    name: 'MuiRating',
    props: inProps
  });
  const {
    className,
    defaultValue = null,
    disabled = false,
    emptyIcon = defaultEmptyIcon,
    emptyLabelText = 'Empty',
    getLabelText = defaultLabelText,
    highlightSelectedOnly = false,
    icon = defaultIcon,
    IconContainerComponent = IconContainer,
    max = 5,
    name: nameProp,
    onChange,
    onChangeActive,
    onMouseLeave,
    onMouseMove,
    precision = 1,
    readOnly = false,
    size = 'medium',
    value: valueProp,
    ...other
  } = props;
  const name = (0, _utils.unstable_useId)(nameProp);
  const [valueDerived, setValueState] = (0, _utils.useControlled)({
    controlled: valueProp,
    default: defaultValue,
    name: 'Rating'
  });
  const valueRounded = roundValueToPrecision(valueDerived, precision);
  const isRtl = (0, _RtlProvider.useRtl)();
  const [{
    hover,
    focus
  }, setState] = React.useState({
    hover: -1,
    focus: -1
  });
  let value = valueRounded;
  if (hover !== -1) {
    value = hover;
  }
  if (focus !== -1) {
    value = focus;
  }
  const [focusVisible, setFocusVisible] = React.useState(false);
  const rootRef = React.useRef();
  const handleRef = (0, _utils.useForkRef)(rootRef, ref);
  const handleMouseMove = event => {
    if (onMouseMove) {
      onMouseMove(event);
    }
    const rootNode = rootRef.current;
    const {
      right,
      left,
      width: containerWidth
    } = rootNode.getBoundingClientRect();
    let percent;
    if (isRtl) {
      percent = (right - event.clientX) / containerWidth;
    } else {
      percent = (event.clientX - left) / containerWidth;
    }
    let newHover = roundValueToPrecision(max * percent + precision / 2, precision);
    newHover = (0, _clamp.default)(newHover, precision, max);
    setState(prev => prev.hover === newHover && prev.focus === newHover ? prev : {
      hover: newHover,
      focus: newHover
    });
    setFocusVisible(false);
    if (onChangeActive && hover !== newHover) {
      onChangeActive(event, newHover);
    }
  };
  const handleMouseLeave = event => {
    if (onMouseLeave) {
      onMouseLeave(event);
    }
    const newHover = -1;
    setState({
      hover: newHover,
      focus: newHover
    });
    if (onChangeActive && hover !== newHover) {
      onChangeActive(event, newHover);
    }
  };
  const handleChange = event => {
    let newValue = event.target.value === '' ? null : parseFloat(event.target.value);

    // Give mouse priority over keyboard
    // Fix https://github.com/mui/material-ui/issues/22827
    if (hover !== -1) {
      newValue = hover;
    }
    setValueState(newValue);
    if (onChange) {
      onChange(event, newValue);
    }
  };
  const handleClear = event => {
    // Ignore keyboard events
    // https://github.com/facebook/react/issues/7407
    if (event.clientX === 0 && event.clientY === 0) {
      return;
    }
    setState({
      hover: -1,
      focus: -1
    });
    setValueState(null);
    if (onChange && parseFloat(event.target.value) === valueRounded) {
      onChange(event, null);
    }
  };
  const handleFocus = event => {
    if ((0, _isFocusVisible.default)(event.target)) {
      setFocusVisible(true);
    }
    const newFocus = parseFloat(event.target.value);
    setState(prev => ({
      hover: prev.hover,
      focus: newFocus
    }));
  };
  const handleBlur = event => {
    if (hover !== -1) {
      return;
    }
    if (!(0, _isFocusVisible.default)(event.target)) {
      setFocusVisible(false);
    }
    const newFocus = -1;
    setState(prev => ({
      hover: prev.hover,
      focus: newFocus
    }));
  };
  const [emptyValueFocused, setEmptyValueFocused] = React.useState(false);
  const ownerState = {
    ...props,
    defaultValue,
    disabled,
    emptyIcon,
    emptyLabelText,
    emptyValueFocused,
    focusVisible,
    getLabelText,
    icon,
    IconContainerComponent,
    max,
    precision,
    readOnly,
    size
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(RatingRoot, {
    ref: handleRef,
    onMouseMove: handleMouseMove,
    onMouseLeave: handleMouseLeave,
    className: (0, _clsx.default)(classes.root, className, readOnly && 'MuiRating-readOnly'),
    ownerState: ownerState,
    role: readOnly ? 'img' : null,
    "aria-label": readOnly ? getLabelText(value) : null,
    ...other,
    children: [Array.from(new Array(max)).map((_, index) => {
      const itemValue = index + 1;
      const ratingItemProps = {
        classes,
        disabled,
        emptyIcon,
        focus,
        getLabelText,
        highlightSelectedOnly,
        hover,
        icon,
        IconContainerComponent,
        name,
        onBlur: handleBlur,
        onChange: handleChange,
        onClick: handleClear,
        onFocus: handleFocus,
        ratingValue: value,
        ratingValueRounded: valueRounded,
        readOnly,
        ownerState
      };
      const isActive = itemValue === Math.ceil(value) && (hover !== -1 || focus !== -1);
      if (precision < 1) {
        const items = Array.from(new Array(1 / precision));
        return /*#__PURE__*/(0, _jsxRuntime.jsx)(RatingDecimal, {
          className: (0, _clsx.default)(classes.decimal, isActive && classes.iconActive),
          ownerState: ownerState,
          iconActive: isActive,
          children: items.map(($, indexDecimal) => {
            const itemDecimalValue = roundValueToPrecision(itemValue - 1 + (indexDecimal + 1) * precision, precision);
            return /*#__PURE__*/(0, _jsxRuntime.jsx)(RatingItem, {
              ...ratingItemProps,
              // The icon is already displayed as active
              isActive: false,
              itemValue: itemDecimalValue,
              labelProps: {
                style: items.length - 1 === indexDecimal ? {} : {
                  width: itemDecimalValue === value ? `${(indexDecimal + 1) * precision * 100}%` : '0%',
                  overflow: 'hidden',
                  position: 'absolute'
                }
              }
            }, itemDecimalValue);
          })
        }, itemValue);
      }
      return /*#__PURE__*/(0, _jsxRuntime.jsx)(RatingItem, {
        ...ratingItemProps,
        isActive: isActive,
        itemValue: itemValue
      }, itemValue);
    }), !readOnly && !disabled && /*#__PURE__*/(0, _jsxRuntime.jsxs)(RatingLabel, {
      className: (0, _clsx.default)(classes.label, classes.labelEmptyValue),
      ownerState: ownerState,
      children: [/*#__PURE__*/(0, _jsxRuntime.jsx)("input", {
        className: classes.visuallyHidden,
        value: "",
        id: `${name}-empty`,
        type: "radio",
        name: name,
        checked: valueRounded == null,
        onFocus: () => setEmptyValueFocused(true),
        onBlur: () => setEmptyValueFocused(false),
        onChange: handleChange
      }), /*#__PURE__*/(0, _jsxRuntime.jsx)("span", {
        className: classes.visuallyHidden,
        children: emptyLabelText
      })]
    })]
  });
});
process.env.NODE_ENV !== "production" ? Rating.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The default value. Use when the component is not controlled.
   * @default null
   */
  defaultValue: _propTypes.default.number,
  /**
   * If `true`, the component is disabled.
   * @default false
   */
  disabled: _propTypes.default.bool,
  /**
   * The icon to display when empty.
   * @default <StarBorder fontSize="inherit" />
   */
  emptyIcon: _propTypes.default.node,
  /**
   * The label read when the rating input is empty.
   * @default 'Empty'
   */
  emptyLabelText: _propTypes.default.node,
  /**
   * Accepts a function which returns a string value that provides a user-friendly name for the current value of the rating.
   * This is important for screen reader users.
   *
   * For localization purposes, you can use the provided [translations](https://mui.com/material-ui/guides/localization/).
   * @param {number} value The rating label's value to format.
   * @returns {string}
   * @default function defaultLabelText(value) {
   *   return `${value || '0'} Star${value !== 1 ? 's' : ''}`;
   * }
   */
  getLabelText: _propTypes.default.func,
  /**
   * If `true`, only the selected icon will be highlighted.
   * @default false
   */
  highlightSelectedOnly: _propTypes.default.bool,
  /**
   * The icon to display.
   * @default <Star fontSize="inherit" />
   */
  icon: _propTypes.default.node,
  /**
   * The component containing the icon.
   * @default function IconContainer(props) {
   *   const { value, ...other } = props;
   *   return <span {...other} />;
   * }
   */
  IconContainerComponent: _propTypes.default.elementType,
  /**
   * Maximum rating.
   * @default 5
   */
  max: _propTypes.default.number,
  /**
   * The name attribute of the radio `input` elements.
   * This input `name` should be unique within the page.
   * Being unique within a form is insufficient since the `name` is used to generate IDs.
   */
  name: _propTypes.default.string,
  /**
   * Callback fired when the value changes.
   * @param {React.SyntheticEvent} event The event source of the callback.
   * @param {number|null} value The new value.
   */
  onChange: _propTypes.default.func,
  /**
   * Callback function that is fired when the hover state changes.
   * @param {React.SyntheticEvent} event The event source of the callback.
   * @param {number} value The new value.
   */
  onChangeActive: _propTypes.default.func,
  /**
   * @ignore
   */
  onMouseLeave: _propTypes.default.func,
  /**
   * @ignore
   */
  onMouseMove: _propTypes.default.func,
  /**
   * The minimum increment value change allowed.
   * @default 1
   */
  precision: (0, _chainPropTypes.default)(_propTypes.default.number, props => {
    if (props.precision < 0.1) {
      return new Error(['MUI: The prop `precision` should be above 0.1.', 'A value below this limit has an imperceptible impact.'].join('\n'));
    }
    return null;
  }),
  /**
   * Removes all hover effects and pointer events.
   * @default false
   */
  readOnly: _propTypes.default.bool,
  /**
   * The size of the component.
   * @default 'medium'
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['small', 'medium', 'large']), _propTypes.default.string]),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The rating value.
   */
  value: _propTypes.default.number
} : void 0;
var _default = exports.default = Rating;