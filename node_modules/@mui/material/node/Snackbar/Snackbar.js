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
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _useSlotProps = _interopRequireDefault(require("@mui/utils/useSlotProps"));
var _useSnackbar = _interopRequireDefault(require("./useSnackbar"));
var _ClickAwayListener = _interopRequireDefault(require("../ClickAwayListener"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _Grow = _interopRequireDefault(require("../Grow"));
var _SnackbarContent = _interopRequireDefault(require("../SnackbarContent"));
var _snackbarClasses = require("./snackbarClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    anchorOrigin
  } = ownerState;
  const slots = {
    root: ['root', `anchorOrigin${(0, _capitalize.default)(anchorOrigin.vertical)}${(0, _capitalize.default)(anchorOrigin.horizontal)}`]
  };
  return (0, _composeClasses.default)(slots, _snackbarClasses.getSnackbarUtilityClass, classes);
};
const SnackbarRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiSnackbar',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[`anchorOrigin${(0, _capitalize.default)(ownerState.anchorOrigin.vertical)}${(0, _capitalize.default)(ownerState.anchorOrigin.horizontal)}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  zIndex: (theme.vars || theme).zIndex.snackbar,
  position: 'fixed',
  display: 'flex',
  left: 8,
  right: 8,
  justifyContent: 'center',
  alignItems: 'center',
  variants: [{
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.vertical === 'top',
    style: {
      top: 8,
      [theme.breakpoints.up('sm')]: {
        top: 24
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.vertical !== 'top',
    style: {
      bottom: 8,
      [theme.breakpoints.up('sm')]: {
        bottom: 24
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.horizontal === 'left',
    style: {
      justifyContent: 'flex-start',
      [theme.breakpoints.up('sm')]: {
        left: 24,
        right: 'auto'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.horizontal === 'right',
    style: {
      justifyContent: 'flex-end',
      [theme.breakpoints.up('sm')]: {
        right: 24,
        left: 'auto'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.horizontal === 'center',
    style: {
      [theme.breakpoints.up('sm')]: {
        left: '50%',
        right: 'auto',
        transform: 'translateX(-50%)'
      }
    }
  }]
})));
const Snackbar = /*#__PURE__*/React.forwardRef(function Snackbar(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiSnackbar'
  });
  const theme = (0, _zeroStyled.useTheme)();
  const defaultTransitionDuration = {
    enter: theme.transitions.duration.enteringScreen,
    exit: theme.transitions.duration.leavingScreen
  };
  const {
    action,
    anchorOrigin: {
      vertical,
      horizontal
    } = {
      vertical: 'bottom',
      horizontal: 'left'
    },
    autoHideDuration = null,
    children,
    className,
    ClickAwayListenerProps,
    ContentProps,
    disableWindowBlurListener = false,
    message,
    onBlur,
    onClose,
    onFocus,
    onMouseEnter,
    onMouseLeave,
    open,
    resumeHideDuration,
    TransitionComponent = _Grow.default,
    transitionDuration = defaultTransitionDuration,
    TransitionProps: {
      onEnter,
      onExited,
      ...TransitionProps
    } = {},
    ...other
  } = props;
  const ownerState = {
    ...props,
    anchorOrigin: {
      vertical,
      horizontal
    },
    autoHideDuration,
    disableWindowBlurListener,
    TransitionComponent,
    transitionDuration
  };
  const classes = useUtilityClasses(ownerState);
  const {
    getRootProps,
    onClickAway
  } = (0, _useSnackbar.default)({
    ...ownerState
  });
  const [exited, setExited] = React.useState(true);
  const rootProps = (0, _useSlotProps.default)({
    elementType: SnackbarRoot,
    getSlotProps: getRootProps,
    externalForwardedProps: other,
    ownerState,
    additionalProps: {
      ref
    },
    className: [classes.root, className]
  });
  const handleExited = node => {
    setExited(true);
    if (onExited) {
      onExited(node);
    }
  };
  const handleEnter = (node, isAppearing) => {
    setExited(false);
    if (onEnter) {
      onEnter(node, isAppearing);
    }
  };

  // So we only render active snackbars.
  if (!open && exited) {
    return null;
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_ClickAwayListener.default, {
    onClickAway: onClickAway,
    ...ClickAwayListenerProps,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(SnackbarRoot, {
      ...rootProps,
      children: /*#__PURE__*/(0, _jsxRuntime.jsx)(TransitionComponent, {
        appear: true,
        in: open,
        timeout: transitionDuration,
        direction: vertical === 'top' ? 'down' : 'up',
        onEnter: handleEnter,
        onExited: handleExited,
        ...TransitionProps,
        children: children || /*#__PURE__*/(0, _jsxRuntime.jsx)(_SnackbarContent.default, {
          message: message,
          action: action,
          ...ContentProps
        })
      })
    })
  });
});
process.env.NODE_ENV !== "production" ? Snackbar.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The action to display. It renders after the message, at the end of the snackbar.
   */
  action: _propTypes.default.node,
  /**
   * The anchor of the `Snackbar`.
   * On smaller screens, the component grows to occupy all the available width,
   * the horizontal alignment is ignored.
   * @default { vertical: 'bottom', horizontal: 'left' }
   */
  anchorOrigin: _propTypes.default.shape({
    horizontal: _propTypes.default.oneOf(['center', 'left', 'right']).isRequired,
    vertical: _propTypes.default.oneOf(['bottom', 'top']).isRequired
  }),
  /**
   * The number of milliseconds to wait before automatically calling the
   * `onClose` function. `onClose` should then set the state of the `open`
   * prop to hide the Snackbar. This behavior is disabled by default with
   * the `null` value.
   * @default null
   */
  autoHideDuration: _propTypes.default.number,
  /**
   * Replace the `SnackbarContent` component.
   */
  children: _propTypes.default.element,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * Props applied to the `ClickAwayListener` element.
   */
  ClickAwayListenerProps: _propTypes.default.object,
  /**
   * Props applied to the [`SnackbarContent`](https://mui.com/material-ui/api/snackbar-content/) element.
   */
  ContentProps: _propTypes.default.object,
  /**
   * If `true`, the `autoHideDuration` timer will expire even if the window is not focused.
   * @default false
   */
  disableWindowBlurListener: _propTypes.default.bool,
  /**
   * When displaying multiple consecutive snackbars using a single parent-rendered
   * `<Snackbar/>`, add the `key` prop to ensure independent treatment of each message.
   * For instance, use `<Snackbar key={message} />`. Otherwise, messages might update
   * in place, and features like `autoHideDuration` could be affected.
   */
  key: () => null,
  /**
   * The message to display.
   */
  message: _propTypes.default.node,
  /**
   * @ignore
   */
  onBlur: _propTypes.default.func,
  /**
   * Callback fired when the component requests to be closed.
   * Typically `onClose` is used to set state in the parent component,
   * which is used to control the `Snackbar` `open` prop.
   * The `reason` parameter can optionally be used to control the response to `onClose`,
   * for example ignoring `clickaway`.
   *
   * @param {React.SyntheticEvent<any> | Event} event The event source of the callback.
   * @param {string} reason Can be: `"timeout"` (`autoHideDuration` expired), `"clickaway"`, or `"escapeKeyDown"`.
   */
  onClose: _propTypes.default.func,
  /**
   * @ignore
   */
  onFocus: _propTypes.default.func,
  /**
   * @ignore
   */
  onMouseEnter: _propTypes.default.func,
  /**
   * @ignore
   */
  onMouseLeave: _propTypes.default.func,
  /**
   * If `true`, the component is shown.
   */
  open: _propTypes.default.bool,
  /**
   * The number of milliseconds to wait before dismissing after user interaction.
   * If `autoHideDuration` prop isn't specified, it does nothing.
   * If `autoHideDuration` prop is specified but `resumeHideDuration` isn't,
   * we default to `autoHideDuration / 2` ms.
   */
  resumeHideDuration: _propTypes.default.number,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The component used for the transition.
   * [Follow this guide](https://mui.com/material-ui/transitions/#transitioncomponent-prop) to learn more about the requirements for this component.
   * @default Grow
   */
  TransitionComponent: _propTypes.default.elementType,
  /**
   * The duration for the transition, in milliseconds.
   * You may specify a single timeout for all transitions, or individually with an object.
   * @default {
   *   enter: theme.transitions.duration.enteringScreen,
   *   exit: theme.transitions.duration.leavingScreen,
   * }
   */
  transitionDuration: _propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.shape({
    appear: _propTypes.default.number,
    enter: _propTypes.default.number,
    exit: _propTypes.default.number
  })]),
  /**
   * Props applied to the transition element.
   * By default, the element is based on this [`Transition`](https://reactcommunity.org/react-transition-group/transition/) component.
   * @default {}
   */
  TransitionProps: _propTypes.default.object
} : void 0;
var _default = exports.default = Snackbar;