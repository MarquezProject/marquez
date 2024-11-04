"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _clsx = _interopRequireDefault(require("clsx"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _reactTransitionGroup = require("react-transition-group");
var _useTimeout = _interopRequireDefault(require("@mui/utils/useTimeout"));
var _elementTypeAcceptingRef = _interopRequireDefault(require("@mui/utils/elementTypeAcceptingRef"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _createTransitions = require("../styles/createTransitions");
var _utils = require("../transitions/utils");
var _utils2 = require("../utils");
var _collapseClasses = require("./collapseClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    orientation,
    classes
  } = ownerState;
  const slots = {
    root: ['root', `${orientation}`],
    entered: ['entered'],
    hidden: ['hidden'],
    wrapper: ['wrapper', `${orientation}`],
    wrapperInner: ['wrapperInner', `${orientation}`]
  };
  return (0, _composeClasses.default)(slots, _collapseClasses.getCollapseUtilityClass, classes);
};
const CollapseRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiCollapse',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[ownerState.orientation], ownerState.state === 'entered' && styles.entered, ownerState.state === 'exited' && !ownerState.in && ownerState.collapsedSize === '0px' && styles.hidden];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  height: 0,
  overflow: 'hidden',
  transition: theme.transitions.create('height'),
  variants: [{
    props: {
      orientation: 'horizontal'
    },
    style: {
      height: 'auto',
      width: 0,
      transition: theme.transitions.create('width')
    }
  }, {
    props: {
      state: 'entered'
    },
    style: {
      height: 'auto',
      overflow: 'visible'
    }
  }, {
    props: {
      state: 'entered',
      orientation: 'horizontal'
    },
    style: {
      width: 'auto'
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.state === 'exited' && !ownerState.in && ownerState.collapsedSize === '0px',
    style: {
      visibility: 'hidden'
    }
  }]
})));
const CollapseWrapper = (0, _zeroStyled.styled)('div', {
  name: 'MuiCollapse',
  slot: 'Wrapper',
  overridesResolver: (props, styles) => styles.wrapper
})({
  // Hack to get children with a negative margin to not falsify the height computation.
  display: 'flex',
  width: '100%',
  variants: [{
    props: {
      orientation: 'horizontal'
    },
    style: {
      width: 'auto',
      height: '100%'
    }
  }]
});
const CollapseWrapperInner = (0, _zeroStyled.styled)('div', {
  name: 'MuiCollapse',
  slot: 'WrapperInner',
  overridesResolver: (props, styles) => styles.wrapperInner
})({
  width: '100%',
  variants: [{
    props: {
      orientation: 'horizontal'
    },
    style: {
      width: 'auto',
      height: '100%'
    }
  }]
});

/**
 * The Collapse transition is used by the
 * [Vertical Stepper](/material-ui/react-stepper/#vertical-stepper) StepContent component.
 * It uses [react-transition-group](https://github.com/reactjs/react-transition-group) internally.
 */
const Collapse = /*#__PURE__*/React.forwardRef(function Collapse(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiCollapse'
  });
  const {
    addEndListener,
    children,
    className,
    collapsedSize: collapsedSizeProp = '0px',
    component,
    easing,
    in: inProp,
    onEnter,
    onEntered,
    onEntering,
    onExit,
    onExited,
    onExiting,
    orientation = 'vertical',
    style,
    timeout = _createTransitions.duration.standard,
    // eslint-disable-next-line react/prop-types
    TransitionComponent = _reactTransitionGroup.Transition,
    ...other
  } = props;
  const ownerState = {
    ...props,
    orientation,
    collapsedSize: collapsedSizeProp
  };
  const classes = useUtilityClasses(ownerState);
  const theme = (0, _zeroStyled.useTheme)();
  const timer = (0, _useTimeout.default)();
  const wrapperRef = React.useRef(null);
  const autoTransitionDuration = React.useRef();
  const collapsedSize = typeof collapsedSizeProp === 'number' ? `${collapsedSizeProp}px` : collapsedSizeProp;
  const isHorizontal = orientation === 'horizontal';
  const size = isHorizontal ? 'width' : 'height';
  const nodeRef = React.useRef(null);
  const handleRef = (0, _utils2.useForkRef)(ref, nodeRef);
  const normalizedTransitionCallback = callback => maybeIsAppearing => {
    if (callback) {
      const node = nodeRef.current;

      // onEnterXxx and onExitXxx callbacks have a different arguments.length value.
      if (maybeIsAppearing === undefined) {
        callback(node);
      } else {
        callback(node, maybeIsAppearing);
      }
    }
  };
  const getWrapperSize = () => wrapperRef.current ? wrapperRef.current[isHorizontal ? 'clientWidth' : 'clientHeight'] : 0;
  const handleEnter = normalizedTransitionCallback((node, isAppearing) => {
    if (wrapperRef.current && isHorizontal) {
      // Set absolute position to get the size of collapsed content
      wrapperRef.current.style.position = 'absolute';
    }
    node.style[size] = collapsedSize;
    if (onEnter) {
      onEnter(node, isAppearing);
    }
  });
  const handleEntering = normalizedTransitionCallback((node, isAppearing) => {
    const wrapperSize = getWrapperSize();
    if (wrapperRef.current && isHorizontal) {
      // After the size is read reset the position back to default
      wrapperRef.current.style.position = '';
    }
    const {
      duration: transitionDuration,
      easing: transitionTimingFunction
    } = (0, _utils.getTransitionProps)({
      style,
      timeout,
      easing
    }, {
      mode: 'enter'
    });
    if (timeout === 'auto') {
      const duration2 = theme.transitions.getAutoHeightDuration(wrapperSize);
      node.style.transitionDuration = `${duration2}ms`;
      autoTransitionDuration.current = duration2;
    } else {
      node.style.transitionDuration = typeof transitionDuration === 'string' ? transitionDuration : `${transitionDuration}ms`;
    }
    node.style[size] = `${wrapperSize}px`;
    node.style.transitionTimingFunction = transitionTimingFunction;
    if (onEntering) {
      onEntering(node, isAppearing);
    }
  });
  const handleEntered = normalizedTransitionCallback((node, isAppearing) => {
    node.style[size] = 'auto';
    if (onEntered) {
      onEntered(node, isAppearing);
    }
  });
  const handleExit = normalizedTransitionCallback(node => {
    node.style[size] = `${getWrapperSize()}px`;
    if (onExit) {
      onExit(node);
    }
  });
  const handleExited = normalizedTransitionCallback(onExited);
  const handleExiting = normalizedTransitionCallback(node => {
    const wrapperSize = getWrapperSize();
    const {
      duration: transitionDuration,
      easing: transitionTimingFunction
    } = (0, _utils.getTransitionProps)({
      style,
      timeout,
      easing
    }, {
      mode: 'exit'
    });
    if (timeout === 'auto') {
      // TODO: rename getAutoHeightDuration to something more generic (width support)
      // Actually it just calculates animation duration based on size
      const duration2 = theme.transitions.getAutoHeightDuration(wrapperSize);
      node.style.transitionDuration = `${duration2}ms`;
      autoTransitionDuration.current = duration2;
    } else {
      node.style.transitionDuration = typeof transitionDuration === 'string' ? transitionDuration : `${transitionDuration}ms`;
    }
    node.style[size] = collapsedSize;
    node.style.transitionTimingFunction = transitionTimingFunction;
    if (onExiting) {
      onExiting(node);
    }
  });
  const handleAddEndListener = next => {
    if (timeout === 'auto') {
      timer.start(autoTransitionDuration.current || 0, next);
    }
    if (addEndListener) {
      // Old call signature before `react-transition-group` implemented `nodeRef`
      addEndListener(nodeRef.current, next);
    }
  };
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(TransitionComponent, {
    in: inProp,
    onEnter: handleEnter,
    onEntered: handleEntered,
    onEntering: handleEntering,
    onExit: handleExit,
    onExited: handleExited,
    onExiting: handleExiting,
    addEndListener: handleAddEndListener,
    nodeRef: nodeRef,
    timeout: timeout === 'auto' ? null : timeout,
    ...other,
    children: (state, childProps) => /*#__PURE__*/(0, _jsxRuntime.jsx)(CollapseRoot, {
      as: component,
      className: (0, _clsx.default)(classes.root, className, {
        'entered': classes.entered,
        'exited': !inProp && collapsedSize === '0px' && classes.hidden
      }[state]),
      style: {
        [isHorizontal ? 'minWidth' : 'minHeight']: collapsedSize,
        ...style
      },
      ref: handleRef,
      ...childProps,
      // `ownerState` is set after `childProps` to override any existing `ownerState` property in `childProps`
      // that might have been forwarded from the Transition component.
      ownerState: {
        ...ownerState,
        state
      },
      children: /*#__PURE__*/(0, _jsxRuntime.jsx)(CollapseWrapper, {
        ownerState: {
          ...ownerState,
          state
        },
        className: classes.wrapper,
        ref: wrapperRef,
        children: /*#__PURE__*/(0, _jsxRuntime.jsx)(CollapseWrapperInner, {
          ownerState: {
            ...ownerState,
            state
          },
          className: classes.wrapperInner,
          children: children
        })
      })
    })
  });
});
process.env.NODE_ENV !== "production" ? Collapse.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Add a custom transition end trigger. Called with the transitioning DOM
   * node and a done callback. Allows for more fine grained transition end
   * logic. Note: Timeouts are still used as a fallback if provided.
   */
  addEndListener: _propTypes.default.func,
  /**
   * The content node to be collapsed.
   */
  children: _propTypes.default.node,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The width (horizontal) or height (vertical) of the container when collapsed.
   * @default '0px'
   */
  collapsedSize: _propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.string]),
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _elementTypeAcceptingRef.default,
  /**
   * The transition timing function.
   * You may specify a single easing or a object containing enter and exit values.
   */
  easing: _propTypes.default.oneOfType([_propTypes.default.shape({
    enter: _propTypes.default.string,
    exit: _propTypes.default.string
  }), _propTypes.default.string]),
  /**
   * If `true`, the component will transition in.
   */
  in: _propTypes.default.bool,
  /**
   * @ignore
   */
  onEnter: _propTypes.default.func,
  /**
   * @ignore
   */
  onEntered: _propTypes.default.func,
  /**
   * @ignore
   */
  onEntering: _propTypes.default.func,
  /**
   * @ignore
   */
  onExit: _propTypes.default.func,
  /**
   * @ignore
   */
  onExited: _propTypes.default.func,
  /**
   * @ignore
   */
  onExiting: _propTypes.default.func,
  /**
   * The transition orientation.
   * @default 'vertical'
   */
  orientation: _propTypes.default.oneOf(['horizontal', 'vertical']),
  /**
   * @ignore
   */
  style: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The duration for the transition, in milliseconds.
   * You may specify a single timeout for all transitions, or individually with an object.
   *
   * Set to 'auto' to automatically calculate transition time based on height.
   * @default duration.standard
   */
  timeout: _propTypes.default.oneOfType([_propTypes.default.oneOf(['auto']), _propTypes.default.number, _propTypes.default.shape({
    appear: _propTypes.default.number,
    enter: _propTypes.default.number,
    exit: _propTypes.default.number
  })])
} : void 0;
if (Collapse) {
  Collapse.muiSupportAuto = true;
}
var _default = exports.default = Collapse;