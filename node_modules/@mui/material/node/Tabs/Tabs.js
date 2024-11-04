"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _reactIs = require("react-is");
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _refType = _interopRequireDefault(require("@mui/utils/refType"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _RtlProvider = require("@mui/system/RtlProvider");
var _useSlotProps = _interopRequireDefault(require("@mui/utils/useSlotProps"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _debounce = _interopRequireDefault(require("../utils/debounce"));
var _animate = _interopRequireDefault(require("../internal/animate"));
var _ScrollbarSize = _interopRequireDefault(require("./ScrollbarSize"));
var _TabScrollButton = _interopRequireDefault(require("../TabScrollButton"));
var _useEventCallback = _interopRequireDefault(require("../utils/useEventCallback"));
var _tabsClasses = _interopRequireWildcard(require("./tabsClasses"));
var _ownerDocument = _interopRequireDefault(require("../utils/ownerDocument"));
var _ownerWindow = _interopRequireDefault(require("../utils/ownerWindow"));
var _jsxRuntime = require("react/jsx-runtime");
const nextItem = (list, item) => {
  if (list === item) {
    return list.firstChild;
  }
  if (item && item.nextElementSibling) {
    return item.nextElementSibling;
  }
  return list.firstChild;
};
const previousItem = (list, item) => {
  if (list === item) {
    return list.lastChild;
  }
  if (item && item.previousElementSibling) {
    return item.previousElementSibling;
  }
  return list.lastChild;
};
const moveFocus = (list, currentFocus, traversalFunction) => {
  let wrappedOnce = false;
  let nextFocus = traversalFunction(list, currentFocus);
  while (nextFocus) {
    // Prevent infinite loop.
    if (nextFocus === list.firstChild) {
      if (wrappedOnce) {
        return;
      }
      wrappedOnce = true;
    }

    // Same logic as useAutocomplete.js
    const nextFocusDisabled = nextFocus.disabled || nextFocus.getAttribute('aria-disabled') === 'true';
    if (!nextFocus.hasAttribute('tabindex') || nextFocusDisabled) {
      // Move to the next element.
      nextFocus = traversalFunction(list, nextFocus);
    } else {
      nextFocus.focus();
      return;
    }
  }
};
const useUtilityClasses = ownerState => {
  const {
    vertical,
    fixed,
    hideScrollbar,
    scrollableX,
    scrollableY,
    centered,
    scrollButtonsHideMobile,
    classes
  } = ownerState;
  const slots = {
    root: ['root', vertical && 'vertical'],
    scroller: ['scroller', fixed && 'fixed', hideScrollbar && 'hideScrollbar', scrollableX && 'scrollableX', scrollableY && 'scrollableY'],
    flexContainer: ['flexContainer', vertical && 'flexContainerVertical', centered && 'centered'],
    indicator: ['indicator'],
    scrollButtons: ['scrollButtons', scrollButtonsHideMobile && 'scrollButtonsHideMobile'],
    scrollableX: [scrollableX && 'scrollableX'],
    hideScrollbar: [hideScrollbar && 'hideScrollbar']
  };
  return (0, _composeClasses.default)(slots, _tabsClasses.getTabsUtilityClass, classes);
};
const TabsRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiTabs',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [{
      [`& .${_tabsClasses.default.scrollButtons}`]: styles.scrollButtons
    }, {
      [`& .${_tabsClasses.default.scrollButtons}`]: ownerState.scrollButtonsHideMobile && styles.scrollButtonsHideMobile
    }, styles.root, ownerState.vertical && styles.vertical];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  overflow: 'hidden',
  minHeight: 48,
  // Add iOS momentum scrolling for iOS < 13.0
  WebkitOverflowScrolling: 'touch',
  display: 'flex',
  variants: [{
    props: ({
      ownerState
    }) => ownerState.vertical,
    style: {
      flexDirection: 'column'
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.scrollButtonsHideMobile,
    style: {
      [`& .${_tabsClasses.default.scrollButtons}`]: {
        [theme.breakpoints.down('sm')]: {
          display: 'none'
        }
      }
    }
  }]
})));
const TabsScroller = (0, _zeroStyled.styled)('div', {
  name: 'MuiTabs',
  slot: 'Scroller',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.scroller, ownerState.fixed && styles.fixed, ownerState.hideScrollbar && styles.hideScrollbar, ownerState.scrollableX && styles.scrollableX, ownerState.scrollableY && styles.scrollableY];
  }
})({
  position: 'relative',
  display: 'inline-block',
  flex: '1 1 auto',
  whiteSpace: 'nowrap',
  variants: [{
    props: ({
      ownerState
    }) => ownerState.fixed,
    style: {
      overflowX: 'hidden',
      width: '100%'
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.hideScrollbar,
    style: {
      // Hide dimensionless scrollbar on macOS
      scrollbarWidth: 'none',
      // Firefox
      '&::-webkit-scrollbar': {
        display: 'none' // Safari + Chrome
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.scrollableX,
    style: {
      overflowX: 'auto',
      overflowY: 'hidden'
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.scrollableY,
    style: {
      overflowY: 'auto',
      overflowX: 'hidden'
    }
  }]
});
const FlexContainer = (0, _zeroStyled.styled)('div', {
  name: 'MuiTabs',
  slot: 'FlexContainer',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.flexContainer, ownerState.vertical && styles.flexContainerVertical, ownerState.centered && styles.centered];
  }
})({
  display: 'flex',
  variants: [{
    props: ({
      ownerState
    }) => ownerState.vertical,
    style: {
      flexDirection: 'column'
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.centered,
    style: {
      justifyContent: 'center'
    }
  }]
});
const TabsIndicator = (0, _zeroStyled.styled)('span', {
  name: 'MuiTabs',
  slot: 'Indicator',
  overridesResolver: (props, styles) => styles.indicator
})((0, _memoTheme.default)(({
  theme
}) => ({
  position: 'absolute',
  height: 2,
  bottom: 0,
  width: '100%',
  transition: theme.transitions.create(),
  variants: [{
    props: {
      indicatorColor: 'primary'
    },
    style: {
      backgroundColor: (theme.vars || theme).palette.primary.main
    }
  }, {
    props: {
      indicatorColor: 'secondary'
    },
    style: {
      backgroundColor: (theme.vars || theme).palette.secondary.main
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.vertical,
    style: {
      height: '100%',
      width: 2,
      right: 0
    }
  }]
})));
const TabsScrollbarSize = (0, _zeroStyled.styled)(_ScrollbarSize.default)({
  overflowX: 'auto',
  overflowY: 'hidden',
  // Hide dimensionless scrollbar on macOS
  scrollbarWidth: 'none',
  // Firefox
  '&::-webkit-scrollbar': {
    display: 'none' // Safari + Chrome
  }
});
const defaultIndicatorStyle = {};
let warnedOnceTabPresent = false;
const Tabs = /*#__PURE__*/React.forwardRef(function Tabs(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiTabs'
  });
  const theme = (0, _zeroStyled.useTheme)();
  const isRtl = (0, _RtlProvider.useRtl)();
  const {
    'aria-label': ariaLabel,
    'aria-labelledby': ariaLabelledBy,
    action,
    centered = false,
    children: childrenProp,
    className,
    component = 'div',
    allowScrollButtonsMobile = false,
    indicatorColor = 'primary',
    onChange,
    orientation = 'horizontal',
    ScrollButtonComponent = _TabScrollButton.default,
    scrollButtons = 'auto',
    selectionFollowsFocus,
    slots = {},
    slotProps = {},
    TabIndicatorProps = {},
    TabScrollButtonProps = {},
    textColor = 'primary',
    value,
    variant = 'standard',
    visibleScrollbar = false,
    ...other
  } = props;
  const scrollable = variant === 'scrollable';
  const vertical = orientation === 'vertical';
  const scrollStart = vertical ? 'scrollTop' : 'scrollLeft';
  const start = vertical ? 'top' : 'left';
  const end = vertical ? 'bottom' : 'right';
  const clientSize = vertical ? 'clientHeight' : 'clientWidth';
  const size = vertical ? 'height' : 'width';
  const ownerState = {
    ...props,
    component,
    allowScrollButtonsMobile,
    indicatorColor,
    orientation,
    vertical,
    scrollButtons,
    textColor,
    variant,
    visibleScrollbar,
    fixed: !scrollable,
    hideScrollbar: scrollable && !visibleScrollbar,
    scrollableX: scrollable && !vertical,
    scrollableY: scrollable && vertical,
    centered: centered && !scrollable,
    scrollButtonsHideMobile: !allowScrollButtonsMobile
  };
  const classes = useUtilityClasses(ownerState);
  const startScrollButtonIconProps = (0, _useSlotProps.default)({
    elementType: slots.StartScrollButtonIcon,
    externalSlotProps: slotProps.startScrollButtonIcon,
    ownerState
  });
  const endScrollButtonIconProps = (0, _useSlotProps.default)({
    elementType: slots.EndScrollButtonIcon,
    externalSlotProps: slotProps.endScrollButtonIcon,
    ownerState
  });
  if (process.env.NODE_ENV !== 'production') {
    if (centered && scrollable) {
      console.error('MUI: You can not use the `centered={true}` and `variant="scrollable"` properties ' + 'at the same time on a `Tabs` component.');
    }
  }
  const [mounted, setMounted] = React.useState(false);
  const [indicatorStyle, setIndicatorStyle] = React.useState(defaultIndicatorStyle);
  const [displayStartScroll, setDisplayStartScroll] = React.useState(false);
  const [displayEndScroll, setDisplayEndScroll] = React.useState(false);
  const [updateScrollObserver, setUpdateScrollObserver] = React.useState(false);
  const [scrollerStyle, setScrollerStyle] = React.useState({
    overflow: 'hidden',
    scrollbarWidth: 0
  });
  const valueToIndex = new Map();
  const tabsRef = React.useRef(null);
  const tabListRef = React.useRef(null);
  const getTabsMeta = () => {
    const tabsNode = tabsRef.current;
    let tabsMeta;
    if (tabsNode) {
      const rect = tabsNode.getBoundingClientRect();
      // create a new object with ClientRect class props + scrollLeft
      tabsMeta = {
        clientWidth: tabsNode.clientWidth,
        scrollLeft: tabsNode.scrollLeft,
        scrollTop: tabsNode.scrollTop,
        scrollWidth: tabsNode.scrollWidth,
        top: rect.top,
        bottom: rect.bottom,
        left: rect.left,
        right: rect.right
      };
    }
    let tabMeta;
    if (tabsNode && value !== false) {
      const children = tabListRef.current.children;
      if (children.length > 0) {
        const tab = children[valueToIndex.get(value)];
        if (process.env.NODE_ENV !== 'production') {
          if (!tab) {
            console.error([`MUI: The \`value\` provided to the Tabs component is invalid.`, `None of the Tabs' children match with "${value}".`, valueToIndex.keys ? `You can provide one of the following values: ${Array.from(valueToIndex.keys()).join(', ')}.` : null].join('\n'));
          }
        }
        tabMeta = tab ? tab.getBoundingClientRect() : null;
        if (process.env.NODE_ENV !== 'production') {
          if (process.env.NODE_ENV !== 'test' && !warnedOnceTabPresent && tabMeta && tabMeta.width === 0 && tabMeta.height === 0 &&
          // if the whole Tabs component is hidden, don't warn
          tabsMeta.clientWidth !== 0) {
            tabsMeta = null;
            console.error(['MUI: The `value` provided to the Tabs component is invalid.', `The Tab with this \`value\` ("${value}") is not part of the document layout.`, "Make sure the tab item is present in the document or that it's not `display: none`."].join('\n'));
            warnedOnceTabPresent = true;
          }
        }
      }
    }
    return {
      tabsMeta,
      tabMeta
    };
  };
  const updateIndicatorState = (0, _useEventCallback.default)(() => {
    const {
      tabsMeta,
      tabMeta
    } = getTabsMeta();
    let startValue = 0;
    let startIndicator;
    if (vertical) {
      startIndicator = 'top';
      if (tabMeta && tabsMeta) {
        startValue = tabMeta.top - tabsMeta.top + tabsMeta.scrollTop;
      }
    } else {
      startIndicator = isRtl ? 'right' : 'left';
      if (tabMeta && tabsMeta) {
        startValue = (isRtl ? -1 : 1) * (tabMeta[startIndicator] - tabsMeta[startIndicator] + tabsMeta.scrollLeft);
      }
    }
    const newIndicatorStyle = {
      [startIndicator]: startValue,
      // May be wrong until the font is loaded.
      [size]: tabMeta ? tabMeta[size] : 0
    };
    if (typeof indicatorStyle[startIndicator] !== 'number' || typeof indicatorStyle[size] !== 'number') {
      setIndicatorStyle(newIndicatorStyle);
    } else {
      const dStart = Math.abs(indicatorStyle[startIndicator] - newIndicatorStyle[startIndicator]);
      const dSize = Math.abs(indicatorStyle[size] - newIndicatorStyle[size]);
      if (dStart >= 1 || dSize >= 1) {
        setIndicatorStyle(newIndicatorStyle);
      }
    }
  });
  const scroll = (scrollValue, {
    animation = true
  } = {}) => {
    if (animation) {
      (0, _animate.default)(scrollStart, tabsRef.current, scrollValue, {
        duration: theme.transitions.duration.standard
      });
    } else {
      tabsRef.current[scrollStart] = scrollValue;
    }
  };
  const moveTabsScroll = delta => {
    let scrollValue = tabsRef.current[scrollStart];
    if (vertical) {
      scrollValue += delta;
    } else {
      scrollValue += delta * (isRtl ? -1 : 1);
    }
    scroll(scrollValue);
  };
  const getScrollSize = () => {
    const containerSize = tabsRef.current[clientSize];
    let totalSize = 0;
    const children = Array.from(tabListRef.current.children);
    for (let i = 0; i < children.length; i += 1) {
      const tab = children[i];
      if (totalSize + tab[clientSize] > containerSize) {
        // If the first item is longer than the container size, then only scroll
        // by the container size.
        if (i === 0) {
          totalSize = containerSize;
        }
        break;
      }
      totalSize += tab[clientSize];
    }
    return totalSize;
  };
  const handleStartScrollClick = () => {
    moveTabsScroll(-1 * getScrollSize());
  };
  const handleEndScrollClick = () => {
    moveTabsScroll(getScrollSize());
  };

  // TODO Remove <ScrollbarSize /> as browser support for hiding the scrollbar
  // with CSS improves.
  const handleScrollbarSizeChange = React.useCallback(scrollbarWidth => {
    setScrollerStyle({
      overflow: null,
      scrollbarWidth
    });
  }, []);
  const getConditionalElements = () => {
    const conditionalElements = {};
    conditionalElements.scrollbarSizeListener = scrollable ? /*#__PURE__*/(0, _jsxRuntime.jsx)(TabsScrollbarSize, {
      onChange: handleScrollbarSizeChange,
      className: (0, _clsx.default)(classes.scrollableX, classes.hideScrollbar)
    }) : null;
    const scrollButtonsActive = displayStartScroll || displayEndScroll;
    const showScrollButtons = scrollable && (scrollButtons === 'auto' && scrollButtonsActive || scrollButtons === true);
    conditionalElements.scrollButtonStart = showScrollButtons ? /*#__PURE__*/(0, _jsxRuntime.jsx)(ScrollButtonComponent, {
      slots: {
        StartScrollButtonIcon: slots.StartScrollButtonIcon
      },
      slotProps: {
        startScrollButtonIcon: startScrollButtonIconProps
      },
      orientation: orientation,
      direction: isRtl ? 'right' : 'left',
      onClick: handleStartScrollClick,
      disabled: !displayStartScroll,
      ...TabScrollButtonProps,
      className: (0, _clsx.default)(classes.scrollButtons, TabScrollButtonProps.className)
    }) : null;
    conditionalElements.scrollButtonEnd = showScrollButtons ? /*#__PURE__*/(0, _jsxRuntime.jsx)(ScrollButtonComponent, {
      slots: {
        EndScrollButtonIcon: slots.EndScrollButtonIcon
      },
      slotProps: {
        endScrollButtonIcon: endScrollButtonIconProps
      },
      orientation: orientation,
      direction: isRtl ? 'left' : 'right',
      onClick: handleEndScrollClick,
      disabled: !displayEndScroll,
      ...TabScrollButtonProps,
      className: (0, _clsx.default)(classes.scrollButtons, TabScrollButtonProps.className)
    }) : null;
    return conditionalElements;
  };
  const scrollSelectedIntoView = (0, _useEventCallback.default)(animation => {
    const {
      tabsMeta,
      tabMeta
    } = getTabsMeta();
    if (!tabMeta || !tabsMeta) {
      return;
    }
    if (tabMeta[start] < tabsMeta[start]) {
      // left side of button is out of view
      const nextScrollStart = tabsMeta[scrollStart] + (tabMeta[start] - tabsMeta[start]);
      scroll(nextScrollStart, {
        animation
      });
    } else if (tabMeta[end] > tabsMeta[end]) {
      // right side of button is out of view
      const nextScrollStart = tabsMeta[scrollStart] + (tabMeta[end] - tabsMeta[end]);
      scroll(nextScrollStart, {
        animation
      });
    }
  });
  const updateScrollButtonState = (0, _useEventCallback.default)(() => {
    if (scrollable && scrollButtons !== false) {
      setUpdateScrollObserver(!updateScrollObserver);
    }
  });
  React.useEffect(() => {
    const handleResize = (0, _debounce.default)(() => {
      // If the Tabs component is replaced by Suspense with a fallback, the last
      // ResizeObserver's handler that runs because of the change in the layout is trying to
      // access a dom node that is no longer there (as the fallback component is being shown instead).
      // See https://github.com/mui/material-ui/issues/33276
      // TODO: Add tests that will ensure the component is not failing when
      // replaced by Suspense with a fallback, once React is updated to version 18
      if (tabsRef.current) {
        updateIndicatorState();
      }
    });
    let resizeObserver;

    /**
     * @type {MutationCallback}
     */
    const handleMutation = records => {
      records.forEach(record => {
        record.removedNodes.forEach(item => {
          resizeObserver?.unobserve(item);
        });
        record.addedNodes.forEach(item => {
          resizeObserver?.observe(item);
        });
      });
      handleResize();
      updateScrollButtonState();
    };
    const win = (0, _ownerWindow.default)(tabsRef.current);
    win.addEventListener('resize', handleResize);
    let mutationObserver;
    if (typeof ResizeObserver !== 'undefined') {
      resizeObserver = new ResizeObserver(handleResize);
      Array.from(tabListRef.current.children).forEach(child => {
        resizeObserver.observe(child);
      });
    }
    if (typeof MutationObserver !== 'undefined') {
      mutationObserver = new MutationObserver(handleMutation);
      mutationObserver.observe(tabListRef.current, {
        childList: true
      });
    }
    return () => {
      handleResize.clear();
      win.removeEventListener('resize', handleResize);
      mutationObserver?.disconnect();
      resizeObserver?.disconnect();
    };
  }, [updateIndicatorState, updateScrollButtonState]);

  /**
   * Toggle visibility of start and end scroll buttons
   * Using IntersectionObserver on first and last Tabs.
   */
  React.useEffect(() => {
    const tabListChildren = Array.from(tabListRef.current.children);
    const length = tabListChildren.length;
    if (typeof IntersectionObserver !== 'undefined' && length > 0 && scrollable && scrollButtons !== false) {
      const firstTab = tabListChildren[0];
      const lastTab = tabListChildren[length - 1];
      const observerOptions = {
        root: tabsRef.current,
        threshold: 0.99
      };
      const handleScrollButtonStart = entries => {
        setDisplayStartScroll(!entries[0].isIntersecting);
      };
      const firstObserver = new IntersectionObserver(handleScrollButtonStart, observerOptions);
      firstObserver.observe(firstTab);
      const handleScrollButtonEnd = entries => {
        setDisplayEndScroll(!entries[0].isIntersecting);
      };
      const lastObserver = new IntersectionObserver(handleScrollButtonEnd, observerOptions);
      lastObserver.observe(lastTab);
      return () => {
        firstObserver.disconnect();
        lastObserver.disconnect();
      };
    }
    return undefined;
  }, [scrollable, scrollButtons, updateScrollObserver, childrenProp?.length]);
  React.useEffect(() => {
    setMounted(true);
  }, []);
  React.useEffect(() => {
    updateIndicatorState();
  });
  React.useEffect(() => {
    // Don't animate on the first render.
    scrollSelectedIntoView(defaultIndicatorStyle !== indicatorStyle);
  }, [scrollSelectedIntoView, indicatorStyle]);
  React.useImperativeHandle(action, () => ({
    updateIndicator: updateIndicatorState,
    updateScrollButtons: updateScrollButtonState
  }), [updateIndicatorState, updateScrollButtonState]);
  const indicator = /*#__PURE__*/(0, _jsxRuntime.jsx)(TabsIndicator, {
    ...TabIndicatorProps,
    className: (0, _clsx.default)(classes.indicator, TabIndicatorProps.className),
    ownerState: ownerState,
    style: {
      ...indicatorStyle,
      ...TabIndicatorProps.style
    }
  });
  let childIndex = 0;
  const children = React.Children.map(childrenProp, child => {
    if (! /*#__PURE__*/React.isValidElement(child)) {
      return null;
    }
    if (process.env.NODE_ENV !== 'production') {
      if ((0, _reactIs.isFragment)(child)) {
        console.error(["MUI: The Tabs component doesn't accept a Fragment as a child.", 'Consider providing an array instead.'].join('\n'));
      }
    }
    const childValue = child.props.value === undefined ? childIndex : child.props.value;
    valueToIndex.set(childValue, childIndex);
    const selected = childValue === value;
    childIndex += 1;
    return /*#__PURE__*/React.cloneElement(child, {
      fullWidth: variant === 'fullWidth',
      indicator: selected && !mounted && indicator,
      selected,
      selectionFollowsFocus,
      onChange,
      textColor,
      value: childValue,
      ...(childIndex === 1 && value === false && !child.props.tabIndex ? {
        tabIndex: 0
      } : {})
    });
  });
  const handleKeyDown = event => {
    const list = tabListRef.current;
    const currentFocus = (0, _ownerDocument.default)(list).activeElement;
    // Keyboard navigation assumes that [role="tab"] are siblings
    // though we might warn in the future about nested, interactive elements
    // as a a11y violation
    const role = currentFocus.getAttribute('role');
    if (role !== 'tab') {
      return;
    }
    let previousItemKey = orientation === 'horizontal' ? 'ArrowLeft' : 'ArrowUp';
    let nextItemKey = orientation === 'horizontal' ? 'ArrowRight' : 'ArrowDown';
    if (orientation === 'horizontal' && isRtl) {
      // swap previousItemKey with nextItemKey
      previousItemKey = 'ArrowRight';
      nextItemKey = 'ArrowLeft';
    }
    switch (event.key) {
      case previousItemKey:
        event.preventDefault();
        moveFocus(list, currentFocus, previousItem);
        break;
      case nextItemKey:
        event.preventDefault();
        moveFocus(list, currentFocus, nextItem);
        break;
      case 'Home':
        event.preventDefault();
        moveFocus(list, null, nextItem);
        break;
      case 'End':
        event.preventDefault();
        moveFocus(list, null, previousItem);
        break;
      default:
        break;
    }
  };
  const conditionalElements = getConditionalElements();
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(TabsRoot, {
    className: (0, _clsx.default)(classes.root, className),
    ownerState: ownerState,
    ref: ref,
    as: component,
    ...other,
    children: [conditionalElements.scrollButtonStart, conditionalElements.scrollbarSizeListener, /*#__PURE__*/(0, _jsxRuntime.jsxs)(TabsScroller, {
      className: classes.scroller,
      ownerState: ownerState,
      style: {
        overflow: scrollerStyle.overflow,
        [vertical ? `margin${isRtl ? 'Left' : 'Right'}` : 'marginBottom']: visibleScrollbar ? undefined : -scrollerStyle.scrollbarWidth
      },
      ref: tabsRef,
      children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(FlexContainer, {
        "aria-label": ariaLabel,
        "aria-labelledby": ariaLabelledBy,
        "aria-orientation": orientation === 'vertical' ? 'vertical' : null,
        className: classes.flexContainer,
        ownerState: ownerState,
        onKeyDown: handleKeyDown,
        ref: tabListRef,
        role: "tablist",
        children: children
      }), mounted && indicator]
    }), conditionalElements.scrollButtonEnd]
  });
});
process.env.NODE_ENV !== "production" ? Tabs.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Callback fired when the component mounts.
   * This is useful when you want to trigger an action programmatically.
   * It supports two actions: `updateIndicator()` and `updateScrollButtons()`
   *
   * @param {object} actions This object contains all possible actions
   * that can be triggered programmatically.
   */
  action: _refType.default,
  /**
   * If `true`, the scroll buttons aren't forced hidden on mobile.
   * By default the scroll buttons are hidden on mobile and takes precedence over `scrollButtons`.
   * @default false
   */
  allowScrollButtonsMobile: _propTypes.default.bool,
  /**
   * The label for the Tabs as a string.
   */
  'aria-label': _propTypes.default.string,
  /**
   * An id or list of ids separated by a space that label the Tabs.
   */
  'aria-labelledby': _propTypes.default.string,
  /**
   * If `true`, the tabs are centered.
   * This prop is intended for large views.
   * @default false
   */
  centered: _propTypes.default.bool,
  /**
   * The content of the component.
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
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * Determines the color of the indicator.
   * @default 'primary'
   */
  indicatorColor: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['primary', 'secondary']), _propTypes.default.string]),
  /**
   * Callback fired when the value changes.
   *
   * @param {React.SyntheticEvent} event The event source of the callback. **Warning**: This is a generic event not a change event.
   * @param {any} value We default to the index of the child (number)
   */
  onChange: _propTypes.default.func,
  /**
   * The component orientation (layout flow direction).
   * @default 'horizontal'
   */
  orientation: _propTypes.default.oneOf(['horizontal', 'vertical']),
  /**
   * The component used to render the scroll buttons.
   * @default TabScrollButton
   */
  ScrollButtonComponent: _propTypes.default.elementType,
  /**
   * Determine behavior of scroll buttons when tabs are set to scroll:
   *
   * - `auto` will only present them when not all the items are visible.
   * - `true` will always present them.
   * - `false` will never present them.
   *
   * By default the scroll buttons are hidden on mobile.
   * This behavior can be disabled with `allowScrollButtonsMobile`.
   * @default 'auto'
   */
  scrollButtons: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOf(['auto', false, true]),
  /**
   * If `true` the selected tab changes on focus. Otherwise it only
   * changes on activation.
   */
  selectionFollowsFocus: _propTypes.default.bool,
  /**
   * The extra props for the slot components.
   * You can override the existing props or add new ones.
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    endScrollButtonIcon: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    startScrollButtonIcon: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * The components used for each slot inside.
   * @default {}
   */
  slots: _propTypes.default.shape({
    EndScrollButtonIcon: _propTypes.default.elementType,
    StartScrollButtonIcon: _propTypes.default.elementType
  }),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * Props applied to the tab indicator element.
   * @default  {}
   */
  TabIndicatorProps: _propTypes.default.object,
  /**
   * Props applied to the [`TabScrollButton`](https://mui.com/material-ui/api/tab-scroll-button/) element.
   * @default {}
   */
  TabScrollButtonProps: _propTypes.default.object,
  /**
   * Determines the color of the `Tab`.
   * @default 'primary'
   */
  textColor: _propTypes.default.oneOf(['inherit', 'primary', 'secondary']),
  /**
   * The value of the currently selected `Tab`.
   * If you don't want any selected `Tab`, you can set this prop to `false`.
   */
  value: _propTypes.default.any,
  /**
   * Determines additional display behavior of the tabs:
   *
   *  - `scrollable` will invoke scrolling properties and allow for horizontally
   *  scrolling (or swiping) of the tab bar.
   *  - `fullWidth` will make the tabs grow to use all the available space,
   *  which should be used for small views, like on mobile.
   *  - `standard` will render the default state.
   * @default 'standard'
   */
  variant: _propTypes.default.oneOf(['fullWidth', 'scrollable', 'standard']),
  /**
   * If `true`, the scrollbar is visible. It can be useful when displaying
   * a long vertical list of tabs.
   * @default false
   */
  visibleScrollbar: _propTypes.default.bool
} : void 0;
var _default = exports.default = Tabs;