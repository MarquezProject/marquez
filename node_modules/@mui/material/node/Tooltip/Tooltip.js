"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
exports.testReset = testReset;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _useTimeout = _interopRequireWildcard(require("@mui/utils/useTimeout"));
var _elementAcceptingRef = _interopRequireDefault(require("@mui/utils/elementAcceptingRef"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _colorManipulator = require("@mui/system/colorManipulator");
var _RtlProvider = require("@mui/system/RtlProvider");
var _isFocusVisible = _interopRequireDefault(require("@mui/utils/isFocusVisible"));
var _appendOwnerState = _interopRequireDefault(require("@mui/utils/appendOwnerState"));
var _getReactElementRef = _interopRequireDefault(require("@mui/utils/getReactElementRef"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _Grow = _interopRequireDefault(require("../Grow"));
var _Popper = _interopRequireDefault(require("../Popper"));
var _useEventCallback = _interopRequireDefault(require("../utils/useEventCallback"));
var _useForkRef = _interopRequireDefault(require("../utils/useForkRef"));
var _useId = _interopRequireDefault(require("../utils/useId"));
var _useControlled = _interopRequireDefault(require("../utils/useControlled"));
var _tooltipClasses = _interopRequireWildcard(require("./tooltipClasses"));
var _jsxRuntime = require("react/jsx-runtime");
function round(value) {
  return Math.round(value * 1e5) / 1e5;
}
const useUtilityClasses = ownerState => {
  const {
    classes,
    disableInteractive,
    arrow,
    touch,
    placement
  } = ownerState;
  const slots = {
    popper: ['popper', !disableInteractive && 'popperInteractive', arrow && 'popperArrow'],
    tooltip: ['tooltip', arrow && 'tooltipArrow', touch && 'touch', `tooltipPlacement${(0, _capitalize.default)(placement.split('-')[0])}`],
    arrow: ['arrow']
  };
  return (0, _composeClasses.default)(slots, _tooltipClasses.getTooltipUtilityClass, classes);
};
const TooltipPopper = (0, _zeroStyled.styled)(_Popper.default, {
  name: 'MuiTooltip',
  slot: 'Popper',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.popper, !ownerState.disableInteractive && styles.popperInteractive, ownerState.arrow && styles.popperArrow, !ownerState.open && styles.popperClose];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  zIndex: (theme.vars || theme).zIndex.tooltip,
  pointerEvents: 'none',
  variants: [{
    props: ({
      ownerState
    }) => !ownerState.disableInteractive,
    style: {
      pointerEvents: 'auto'
    }
  }, {
    props: ({
      open
    }) => !open,
    style: {
      pointerEvents: 'none'
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.arrow,
    style: {
      [`&[data-popper-placement*="bottom"] .${_tooltipClasses.default.arrow}`]: {
        top: 0,
        marginTop: '-0.71em',
        '&::before': {
          transformOrigin: '0 100%'
        }
      },
      [`&[data-popper-placement*="top"] .${_tooltipClasses.default.arrow}`]: {
        bottom: 0,
        marginBottom: '-0.71em',
        '&::before': {
          transformOrigin: '100% 0'
        }
      },
      [`&[data-popper-placement*="right"] .${_tooltipClasses.default.arrow}`]: {
        height: '1em',
        width: '0.71em',
        '&::before': {
          transformOrigin: '100% 100%'
        }
      },
      [`&[data-popper-placement*="left"] .${_tooltipClasses.default.arrow}`]: {
        height: '1em',
        width: '0.71em',
        '&::before': {
          transformOrigin: '0 0'
        }
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.arrow && !ownerState.isRtl,
    style: {
      [`&[data-popper-placement*="right"] .${_tooltipClasses.default.arrow}`]: {
        left: 0,
        marginLeft: '-0.71em'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.arrow && !!ownerState.isRtl,
    style: {
      [`&[data-popper-placement*="right"] .${_tooltipClasses.default.arrow}`]: {
        right: 0,
        marginRight: '-0.71em'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.arrow && !ownerState.isRtl,
    style: {
      [`&[data-popper-placement*="left"] .${_tooltipClasses.default.arrow}`]: {
        right: 0,
        marginRight: '-0.71em'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.arrow && !!ownerState.isRtl,
    style: {
      [`&[data-popper-placement*="left"] .${_tooltipClasses.default.arrow}`]: {
        left: 0,
        marginLeft: '-0.71em'
      }
    }
  }]
})));
const TooltipTooltip = (0, _zeroStyled.styled)('div', {
  name: 'MuiTooltip',
  slot: 'Tooltip',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.tooltip, ownerState.touch && styles.touch, ownerState.arrow && styles.tooltipArrow, styles[`tooltipPlacement${(0, _capitalize.default)(ownerState.placement.split('-')[0])}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  backgroundColor: theme.vars ? theme.vars.palette.Tooltip.bg : (0, _colorManipulator.alpha)(theme.palette.grey[700], 0.92),
  borderRadius: (theme.vars || theme).shape.borderRadius,
  color: (theme.vars || theme).palette.common.white,
  fontFamily: theme.typography.fontFamily,
  padding: '4px 8px',
  fontSize: theme.typography.pxToRem(11),
  maxWidth: 300,
  margin: 2,
  wordWrap: 'break-word',
  fontWeight: theme.typography.fontWeightMedium,
  [`.${_tooltipClasses.default.popper}[data-popper-placement*="left"] &`]: {
    transformOrigin: 'right center'
  },
  [`.${_tooltipClasses.default.popper}[data-popper-placement*="right"] &`]: {
    transformOrigin: 'left center'
  },
  [`.${_tooltipClasses.default.popper}[data-popper-placement*="top"] &`]: {
    transformOrigin: 'center bottom',
    marginBottom: '14px'
  },
  [`.${_tooltipClasses.default.popper}[data-popper-placement*="bottom"] &`]: {
    transformOrigin: 'center top',
    marginTop: '14px'
  },
  variants: [{
    props: ({
      ownerState
    }) => ownerState.arrow,
    style: {
      position: 'relative',
      margin: 0
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.touch,
    style: {
      padding: '8px 16px',
      fontSize: theme.typography.pxToRem(14),
      lineHeight: `${round(16 / 14)}em`,
      fontWeight: theme.typography.fontWeightRegular
    }
  }, {
    props: ({
      ownerState
    }) => !ownerState.isRtl,
    style: {
      [`.${_tooltipClasses.default.popper}[data-popper-placement*="left"] &`]: {
        marginRight: '14px'
      },
      [`.${_tooltipClasses.default.popper}[data-popper-placement*="right"] &`]: {
        marginLeft: '14px'
      }
    }
  }, {
    props: ({
      ownerState
    }) => !ownerState.isRtl && ownerState.touch,
    style: {
      [`.${_tooltipClasses.default.popper}[data-popper-placement*="left"] &`]: {
        marginRight: '24px'
      },
      [`.${_tooltipClasses.default.popper}[data-popper-placement*="right"] &`]: {
        marginLeft: '24px'
      }
    }
  }, {
    props: ({
      ownerState
    }) => !!ownerState.isRtl,
    style: {
      [`.${_tooltipClasses.default.popper}[data-popper-placement*="left"] &`]: {
        marginLeft: '14px'
      },
      [`.${_tooltipClasses.default.popper}[data-popper-placement*="right"] &`]: {
        marginRight: '14px'
      }
    }
  }, {
    props: ({
      ownerState
    }) => !!ownerState.isRtl && ownerState.touch,
    style: {
      [`.${_tooltipClasses.default.popper}[data-popper-placement*="left"] &`]: {
        marginLeft: '24px'
      },
      [`.${_tooltipClasses.default.popper}[data-popper-placement*="right"] &`]: {
        marginRight: '24px'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.touch,
    style: {
      [`.${_tooltipClasses.default.popper}[data-popper-placement*="top"] &`]: {
        marginBottom: '24px'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.touch,
    style: {
      [`.${_tooltipClasses.default.popper}[data-popper-placement*="bottom"] &`]: {
        marginTop: '24px'
      }
    }
  }]
})));
const TooltipArrow = (0, _zeroStyled.styled)('span', {
  name: 'MuiTooltip',
  slot: 'Arrow',
  overridesResolver: (props, styles) => styles.arrow
})((0, _memoTheme.default)(({
  theme
}) => ({
  overflow: 'hidden',
  position: 'absolute',
  width: '1em',
  height: '0.71em' /* = width / sqrt(2) = (length of the hypotenuse) */,
  boxSizing: 'border-box',
  color: theme.vars ? theme.vars.palette.Tooltip.bg : (0, _colorManipulator.alpha)(theme.palette.grey[700], 0.9),
  '&::before': {
    content: '""',
    margin: 'auto',
    display: 'block',
    width: '100%',
    height: '100%',
    backgroundColor: 'currentColor',
    transform: 'rotate(45deg)'
  }
})));
let hystersisOpen = false;
const hystersisTimer = new _useTimeout.Timeout();
let cursorPosition = {
  x: 0,
  y: 0
};
function testReset() {
  hystersisOpen = false;
  hystersisTimer.clear();
}
function composeEventHandler(handler, eventHandler) {
  return (event, ...params) => {
    if (eventHandler) {
      eventHandler(event, ...params);
    }
    handler(event, ...params);
  };
}

// TODO v6: Remove PopperComponent, PopperProps, TransitionComponent and TransitionProps.
const Tooltip = /*#__PURE__*/React.forwardRef(function Tooltip(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiTooltip'
  });
  const {
    arrow = false,
    children: childrenProp,
    classes: classesProp,
    components = {},
    componentsProps = {},
    describeChild = false,
    disableFocusListener = false,
    disableHoverListener = false,
    disableInteractive: disableInteractiveProp = false,
    disableTouchListener = false,
    enterDelay = 100,
    enterNextDelay = 0,
    enterTouchDelay = 700,
    followCursor = false,
    id: idProp,
    leaveDelay = 0,
    leaveTouchDelay = 1500,
    onClose,
    onOpen,
    open: openProp,
    placement = 'bottom',
    PopperComponent: PopperComponentProp,
    PopperProps = {},
    slotProps = {},
    slots = {},
    title,
    TransitionComponent: TransitionComponentProp = _Grow.default,
    TransitionProps,
    ...other
  } = props;

  // to prevent runtime errors, developers will need to provide a child as a React element anyway.
  const children = /*#__PURE__*/React.isValidElement(childrenProp) ? childrenProp : /*#__PURE__*/(0, _jsxRuntime.jsx)("span", {
    children: childrenProp
  });
  const theme = (0, _zeroStyled.useTheme)();
  const isRtl = (0, _RtlProvider.useRtl)();
  const [childNode, setChildNode] = React.useState();
  const [arrowRef, setArrowRef] = React.useState(null);
  const ignoreNonTouchEvents = React.useRef(false);
  const disableInteractive = disableInteractiveProp || followCursor;
  const closeTimer = (0, _useTimeout.default)();
  const enterTimer = (0, _useTimeout.default)();
  const leaveTimer = (0, _useTimeout.default)();
  const touchTimer = (0, _useTimeout.default)();
  const [openState, setOpenState] = (0, _useControlled.default)({
    controlled: openProp,
    default: false,
    name: 'Tooltip',
    state: 'open'
  });
  let open = openState;
  if (process.env.NODE_ENV !== 'production') {
    // TODO: uncomment once we enable eslint-plugin-react-compiler // eslint-disable-next-line react-compiler/react-compiler
    // eslint-disable-next-line react-hooks/rules-of-hooks -- process.env never changes
    const {
      current: isControlled
    } = React.useRef(openProp !== undefined);

    // TODO: uncomment once we enable eslint-plugin-react-compiler // eslint-disable-next-line react-compiler/react-compiler
    // eslint-disable-next-line react-hooks/rules-of-hooks -- process.env never changes
    React.useEffect(() => {
      if (childNode && childNode.disabled && !isControlled && title !== '' && childNode.tagName.toLowerCase() === 'button') {
        console.error(['MUI: You are providing a disabled `button` child to the Tooltip component.', 'A disabled element does not fire events.', "Tooltip needs to listen to the child element's events to display the title.", '', 'Add a simple wrapper element, such as a `span`.'].join('\n'));
      }
    }, [title, childNode, isControlled]);
  }
  const id = (0, _useId.default)(idProp);
  const prevUserSelect = React.useRef();
  const stopTouchInteraction = (0, _useEventCallback.default)(() => {
    if (prevUserSelect.current !== undefined) {
      document.body.style.WebkitUserSelect = prevUserSelect.current;
      prevUserSelect.current = undefined;
    }
    touchTimer.clear();
  });
  React.useEffect(() => stopTouchInteraction, [stopTouchInteraction]);
  const handleOpen = event => {
    hystersisTimer.clear();
    hystersisOpen = true;

    // The mouseover event will trigger for every nested element in the tooltip.
    // We can skip rerendering when the tooltip is already open.
    // We are using the mouseover event instead of the mouseenter event to fix a hide/show issue.
    setOpenState(true);
    if (onOpen && !open) {
      onOpen(event);
    }
  };
  const handleClose = (0, _useEventCallback.default)(
  /**
   * @param {React.SyntheticEvent | Event} event
   */
  event => {
    hystersisTimer.start(800 + leaveDelay, () => {
      hystersisOpen = false;
    });
    setOpenState(false);
    if (onClose && open) {
      onClose(event);
    }
    closeTimer.start(theme.transitions.duration.shortest, () => {
      ignoreNonTouchEvents.current = false;
    });
  });
  const handleMouseOver = event => {
    if (ignoreNonTouchEvents.current && event.type !== 'touchstart') {
      return;
    }

    // Remove the title ahead of time.
    // We don't want to wait for the next render commit.
    // We would risk displaying two tooltips at the same time (native + this one).
    if (childNode) {
      childNode.removeAttribute('title');
    }
    enterTimer.clear();
    leaveTimer.clear();
    if (enterDelay || hystersisOpen && enterNextDelay) {
      enterTimer.start(hystersisOpen ? enterNextDelay : enterDelay, () => {
        handleOpen(event);
      });
    } else {
      handleOpen(event);
    }
  };
  const handleMouseLeave = event => {
    enterTimer.clear();
    leaveTimer.start(leaveDelay, () => {
      handleClose(event);
    });
  };
  const [, setChildIsFocusVisible] = React.useState(false);
  const handleBlur = event => {
    if (!(0, _isFocusVisible.default)(event.target)) {
      setChildIsFocusVisible(false);
      handleMouseLeave(event);
    }
  };
  const handleFocus = event => {
    // Workaround for https://github.com/facebook/react/issues/7769
    // The autoFocus of React might trigger the event before the componentDidMount.
    // We need to account for this eventuality.
    if (!childNode) {
      setChildNode(event.currentTarget);
    }
    if ((0, _isFocusVisible.default)(event.target)) {
      setChildIsFocusVisible(true);
      handleMouseOver(event);
    }
  };
  const detectTouchStart = event => {
    ignoreNonTouchEvents.current = true;
    const childrenProps = children.props;
    if (childrenProps.onTouchStart) {
      childrenProps.onTouchStart(event);
    }
  };
  const handleTouchStart = event => {
    detectTouchStart(event);
    leaveTimer.clear();
    closeTimer.clear();
    stopTouchInteraction();
    prevUserSelect.current = document.body.style.WebkitUserSelect;
    // Prevent iOS text selection on long-tap.
    document.body.style.WebkitUserSelect = 'none';
    touchTimer.start(enterTouchDelay, () => {
      document.body.style.WebkitUserSelect = prevUserSelect.current;
      handleMouseOver(event);
    });
  };
  const handleTouchEnd = event => {
    if (children.props.onTouchEnd) {
      children.props.onTouchEnd(event);
    }
    stopTouchInteraction();
    leaveTimer.start(leaveTouchDelay, () => {
      handleClose(event);
    });
  };
  React.useEffect(() => {
    if (!open) {
      return undefined;
    }

    /**
     * @param {KeyboardEvent} nativeEvent
     */
    function handleKeyDown(nativeEvent) {
      if (nativeEvent.key === 'Escape') {
        handleClose(nativeEvent);
      }
    }
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [handleClose, open]);
  const handleRef = (0, _useForkRef.default)((0, _getReactElementRef.default)(children), setChildNode, ref);

  // There is no point in displaying an empty tooltip.
  // So we exclude all falsy values, except 0, which is valid.
  if (!title && title !== 0) {
    open = false;
  }
  const popperRef = React.useRef();
  const handleMouseMove = event => {
    const childrenProps = children.props;
    if (childrenProps.onMouseMove) {
      childrenProps.onMouseMove(event);
    }
    cursorPosition = {
      x: event.clientX,
      y: event.clientY
    };
    if (popperRef.current) {
      popperRef.current.update();
    }
  };
  const nameOrDescProps = {};
  const titleIsString = typeof title === 'string';
  if (describeChild) {
    nameOrDescProps.title = !open && titleIsString && !disableHoverListener ? title : null;
    nameOrDescProps['aria-describedby'] = open ? id : null;
  } else {
    nameOrDescProps['aria-label'] = titleIsString ? title : null;
    nameOrDescProps['aria-labelledby'] = open && !titleIsString ? id : null;
  }
  const childrenProps = {
    ...nameOrDescProps,
    ...other,
    ...children.props,
    className: (0, _clsx.default)(other.className, children.props.className),
    onTouchStart: detectTouchStart,
    ref: handleRef,
    ...(followCursor ? {
      onMouseMove: handleMouseMove
    } : {})
  };
  if (process.env.NODE_ENV !== 'production') {
    childrenProps['data-mui-internal-clone-element'] = true;

    // TODO: uncomment once we enable eslint-plugin-react-compiler // eslint-disable-next-line react-compiler/react-compiler
    // eslint-disable-next-line react-hooks/rules-of-hooks -- process.env never changes
    React.useEffect(() => {
      if (childNode && !childNode.getAttribute('data-mui-internal-clone-element')) {
        console.error(['MUI: The `children` component of the Tooltip is not forwarding its props correctly.', 'Please make sure that props are spread on the same element that the ref is applied to.'].join('\n'));
      }
    }, [childNode]);
  }
  const interactiveWrapperListeners = {};
  if (!disableTouchListener) {
    childrenProps.onTouchStart = handleTouchStart;
    childrenProps.onTouchEnd = handleTouchEnd;
  }
  if (!disableHoverListener) {
    childrenProps.onMouseOver = composeEventHandler(handleMouseOver, childrenProps.onMouseOver);
    childrenProps.onMouseLeave = composeEventHandler(handleMouseLeave, childrenProps.onMouseLeave);
    if (!disableInteractive) {
      interactiveWrapperListeners.onMouseOver = handleMouseOver;
      interactiveWrapperListeners.onMouseLeave = handleMouseLeave;
    }
  }
  if (!disableFocusListener) {
    childrenProps.onFocus = composeEventHandler(handleFocus, childrenProps.onFocus);
    childrenProps.onBlur = composeEventHandler(handleBlur, childrenProps.onBlur);
    if (!disableInteractive) {
      interactiveWrapperListeners.onFocus = handleFocus;
      interactiveWrapperListeners.onBlur = handleBlur;
    }
  }
  if (process.env.NODE_ENV !== 'production') {
    if (children.props.title) {
      console.error(['MUI: You have provided a `title` prop to the child of <Tooltip />.', `Remove this title prop \`${children.props.title}\` or the Tooltip component.`].join('\n'));
    }
  }
  const popperOptions = React.useMemo(() => {
    let tooltipModifiers = [{
      name: 'arrow',
      enabled: Boolean(arrowRef),
      options: {
        element: arrowRef,
        padding: 4
      }
    }];
    if (PopperProps.popperOptions?.modifiers) {
      tooltipModifiers = tooltipModifiers.concat(PopperProps.popperOptions.modifiers);
    }
    return {
      ...PopperProps.popperOptions,
      modifiers: tooltipModifiers
    };
  }, [arrowRef, PopperProps]);
  const ownerState = {
    ...props,
    isRtl,
    arrow,
    disableInteractive,
    placement,
    PopperComponentProp,
    touch: ignoreNonTouchEvents.current
  };
  const classes = useUtilityClasses(ownerState);
  const PopperComponent = slots.popper ?? components.Popper ?? TooltipPopper;
  const TransitionComponent = slots.transition ?? components.Transition ?? TransitionComponentProp ?? _Grow.default;
  const TooltipComponent = slots.tooltip ?? components.Tooltip ?? TooltipTooltip;
  const ArrowComponent = slots.arrow ?? components.Arrow ?? TooltipArrow;
  const popperProps = (0, _appendOwnerState.default)(PopperComponent, {
    ...PopperProps,
    ...(slotProps.popper ?? componentsProps.popper),
    className: (0, _clsx.default)(classes.popper, PopperProps?.className, (slotProps.popper ?? componentsProps.popper)?.className)
  }, ownerState);
  const transitionProps = (0, _appendOwnerState.default)(TransitionComponent, {
    ...TransitionProps,
    ...(slotProps.transition ?? componentsProps.transition)
  }, ownerState);
  const tooltipProps = (0, _appendOwnerState.default)(TooltipComponent, {
    ...(slotProps.tooltip ?? componentsProps.tooltip),
    className: (0, _clsx.default)(classes.tooltip, (slotProps.tooltip ?? componentsProps.tooltip)?.className)
  }, ownerState);
  const tooltipArrowProps = (0, _appendOwnerState.default)(ArrowComponent, {
    ...(slotProps.arrow ?? componentsProps.arrow),
    className: (0, _clsx.default)(classes.arrow, (slotProps.arrow ?? componentsProps.arrow)?.className)
  }, ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(React.Fragment, {
    children: [/*#__PURE__*/React.cloneElement(children, childrenProps), /*#__PURE__*/(0, _jsxRuntime.jsx)(PopperComponent, {
      as: PopperComponentProp ?? _Popper.default,
      placement: placement,
      anchorEl: followCursor ? {
        getBoundingClientRect: () => ({
          top: cursorPosition.y,
          left: cursorPosition.x,
          right: cursorPosition.x,
          bottom: cursorPosition.y,
          width: 0,
          height: 0
        })
      } : childNode,
      popperRef: popperRef,
      open: childNode ? open : false,
      id: id,
      transition: true,
      ...interactiveWrapperListeners,
      ...popperProps,
      popperOptions: popperOptions,
      children: ({
        TransitionProps: TransitionPropsInner
      }) => /*#__PURE__*/(0, _jsxRuntime.jsx)(TransitionComponent, {
        timeout: theme.transitions.duration.shorter,
        ...TransitionPropsInner,
        ...transitionProps,
        children: /*#__PURE__*/(0, _jsxRuntime.jsxs)(TooltipComponent, {
          ...tooltipProps,
          children: [title, arrow ? /*#__PURE__*/(0, _jsxRuntime.jsx)(ArrowComponent, {
            ...tooltipArrowProps,
            ref: setArrowRef
          }) : null]
        })
      })
    })]
  });
});
process.env.NODE_ENV !== "production" ? Tooltip.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * If `true`, adds an arrow to the tooltip.
   * @default false
   */
  arrow: _propTypes.default.bool,
  /**
   * Tooltip reference element.
   */
  children: _elementAcceptingRef.default.isRequired,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The components used for each slot inside.
   *
   * @deprecated use the `slots` prop instead. This prop will be removed in v7. [How to migrate](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/).
   *
   * @default {}
   */
  components: _propTypes.default.shape({
    Arrow: _propTypes.default.elementType,
    Popper: _propTypes.default.elementType,
    Tooltip: _propTypes.default.elementType,
    Transition: _propTypes.default.elementType
  }),
  /**
   * The extra props for the slot components.
   * You can override the existing props or add new ones.
   *
   * @deprecated use the `slotProps` prop instead. This prop will be removed in v7. [How to migrate](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/).
   *
   * @default {}
   */
  componentsProps: _propTypes.default.shape({
    arrow: _propTypes.default.object,
    popper: _propTypes.default.object,
    tooltip: _propTypes.default.object,
    transition: _propTypes.default.object
  }),
  /**
   * Set to `true` if the `title` acts as an accessible description.
   * By default the `title` acts as an accessible label for the child.
   * @default false
   */
  describeChild: _propTypes.default.bool,
  /**
   * Do not respond to focus-visible events.
   * @default false
   */
  disableFocusListener: _propTypes.default.bool,
  /**
   * Do not respond to hover events.
   * @default false
   */
  disableHoverListener: _propTypes.default.bool,
  /**
   * Makes a tooltip not interactive, i.e. it will close when the user
   * hovers over the tooltip before the `leaveDelay` is expired.
   * @default false
   */
  disableInteractive: _propTypes.default.bool,
  /**
   * Do not respond to long press touch events.
   * @default false
   */
  disableTouchListener: _propTypes.default.bool,
  /**
   * The number of milliseconds to wait before showing the tooltip.
   * This prop won't impact the enter touch delay (`enterTouchDelay`).
   * @default 100
   */
  enterDelay: _propTypes.default.number,
  /**
   * The number of milliseconds to wait before showing the tooltip when one was already recently opened.
   * @default 0
   */
  enterNextDelay: _propTypes.default.number,
  /**
   * The number of milliseconds a user must touch the element before showing the tooltip.
   * @default 700
   */
  enterTouchDelay: _propTypes.default.number,
  /**
   * If `true`, the tooltip follow the cursor over the wrapped element.
   * @default false
   */
  followCursor: _propTypes.default.bool,
  /**
   * This prop is used to help implement the accessibility logic.
   * If you don't provide this prop. It falls back to a randomly generated id.
   */
  id: _propTypes.default.string,
  /**
   * The number of milliseconds to wait before hiding the tooltip.
   * This prop won't impact the leave touch delay (`leaveTouchDelay`).
   * @default 0
   */
  leaveDelay: _propTypes.default.number,
  /**
   * The number of milliseconds after the user stops touching an element before hiding the tooltip.
   * @default 1500
   */
  leaveTouchDelay: _propTypes.default.number,
  /**
   * Callback fired when the component requests to be closed.
   *
   * @param {React.SyntheticEvent} event The event source of the callback.
   */
  onClose: _propTypes.default.func,
  /**
   * Callback fired when the component requests to be open.
   *
   * @param {React.SyntheticEvent} event The event source of the callback.
   */
  onOpen: _propTypes.default.func,
  /**
   * If `true`, the component is shown.
   */
  open: _propTypes.default.bool,
  /**
   * Tooltip placement.
   * @default 'bottom'
   */
  placement: _propTypes.default.oneOf(['bottom-end', 'bottom-start', 'bottom', 'left-end', 'left-start', 'left', 'right-end', 'right-start', 'right', 'top-end', 'top-start', 'top']),
  /**
   * The component used for the popper.
   * @default Popper
   */
  PopperComponent: _propTypes.default.elementType,
  /**
   * Props applied to the [`Popper`](https://mui.com/material-ui/api/popper/) element.
   * @default {}
   */
  PopperProps: _propTypes.default.object,
  /**
   * The extra props for the slot components.
   * You can override the existing props or add new ones.
   *
   * This prop is an alias for the `componentsProps` prop, which will be deprecated in the future.
   *
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    arrow: _propTypes.default.object,
    popper: _propTypes.default.object,
    tooltip: _propTypes.default.object,
    transition: _propTypes.default.object
  }),
  /**
   * The components used for each slot inside.
   *
   * This prop is an alias for the `components` prop, which will be deprecated in the future.
   *
   * @default {}
   */
  slots: _propTypes.default.shape({
    arrow: _propTypes.default.elementType,
    popper: _propTypes.default.elementType,
    tooltip: _propTypes.default.elementType,
    transition: _propTypes.default.elementType
  }),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * Tooltip title. Zero-length titles string, undefined, null and false are never displayed.
   */
  title: _propTypes.default.node,
  /**
   * The component used for the transition.
   * [Follow this guide](https://mui.com/material-ui/transitions/#transitioncomponent-prop) to learn more about the requirements for this component.
   * @default Grow
   */
  TransitionComponent: _propTypes.default.elementType,
  /**
   * Props applied to the transition element.
   * By default, the element is based on this [`Transition`](https://reactcommunity.org/react-transition-group/transition/) component.
   */
  TransitionProps: _propTypes.default.object
} : void 0;
var _default = exports.default = Tooltip;