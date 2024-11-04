"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.overridesResolver = exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _colorManipulator = require("@mui/system/colorManipulator");
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _rootShouldForwardProp = _interopRequireDefault(require("../styles/rootShouldForwardProp"));
var _ButtonBase = _interopRequireDefault(require("../ButtonBase"));
var _useEnhancedEffect = _interopRequireDefault(require("../utils/useEnhancedEffect"));
var _useForkRef = _interopRequireDefault(require("../utils/useForkRef"));
var _ListContext = _interopRequireDefault(require("../List/ListContext"));
var _listItemButtonClasses = _interopRequireWildcard(require("./listItemButtonClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const overridesResolver = (props, styles) => {
  const {
    ownerState
  } = props;
  return [styles.root, ownerState.dense && styles.dense, ownerState.alignItems === 'flex-start' && styles.alignItemsFlexStart, ownerState.divider && styles.divider, !ownerState.disableGutters && styles.gutters];
};
exports.overridesResolver = overridesResolver;
const useUtilityClasses = ownerState => {
  const {
    alignItems,
    classes,
    dense,
    disabled,
    disableGutters,
    divider,
    selected
  } = ownerState;
  const slots = {
    root: ['root', dense && 'dense', !disableGutters && 'gutters', divider && 'divider', disabled && 'disabled', alignItems === 'flex-start' && 'alignItemsFlexStart', selected && 'selected']
  };
  const composedClasses = (0, _composeClasses.default)(slots, _listItemButtonClasses.getListItemButtonUtilityClass, classes);
  return {
    ...classes,
    ...composedClasses
  };
};
const ListItemButtonRoot = (0, _zeroStyled.styled)(_ButtonBase.default, {
  shouldForwardProp: prop => (0, _rootShouldForwardProp.default)(prop) || prop === 'classes',
  name: 'MuiListItemButton',
  slot: 'Root',
  overridesResolver
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'flex',
  flexGrow: 1,
  justifyContent: 'flex-start',
  alignItems: 'center',
  position: 'relative',
  textDecoration: 'none',
  minWidth: 0,
  boxSizing: 'border-box',
  textAlign: 'left',
  paddingTop: 8,
  paddingBottom: 8,
  transition: theme.transitions.create('background-color', {
    duration: theme.transitions.duration.shortest
  }),
  '&:hover': {
    textDecoration: 'none',
    backgroundColor: (theme.vars || theme).palette.action.hover,
    // Reset on touch devices, it doesn't add specificity
    '@media (hover: none)': {
      backgroundColor: 'transparent'
    }
  },
  [`&.${_listItemButtonClasses.default.selected}`]: {
    backgroundColor: theme.vars ? `rgba(${theme.vars.palette.primary.mainChannel} / ${theme.vars.palette.action.selectedOpacity})` : (0, _colorManipulator.alpha)(theme.palette.primary.main, theme.palette.action.selectedOpacity),
    [`&.${_listItemButtonClasses.default.focusVisible}`]: {
      backgroundColor: theme.vars ? `rgba(${theme.vars.palette.primary.mainChannel} / calc(${theme.vars.palette.action.selectedOpacity} + ${theme.vars.palette.action.focusOpacity}))` : (0, _colorManipulator.alpha)(theme.palette.primary.main, theme.palette.action.selectedOpacity + theme.palette.action.focusOpacity)
    }
  },
  [`&.${_listItemButtonClasses.default.selected}:hover`]: {
    backgroundColor: theme.vars ? `rgba(${theme.vars.palette.primary.mainChannel} / calc(${theme.vars.palette.action.selectedOpacity} + ${theme.vars.palette.action.hoverOpacity}))` : (0, _colorManipulator.alpha)(theme.palette.primary.main, theme.palette.action.selectedOpacity + theme.palette.action.hoverOpacity),
    // Reset on touch devices, it doesn't add specificity
    '@media (hover: none)': {
      backgroundColor: theme.vars ? `rgba(${theme.vars.palette.primary.mainChannel} / ${theme.vars.palette.action.selectedOpacity})` : (0, _colorManipulator.alpha)(theme.palette.primary.main, theme.palette.action.selectedOpacity)
    }
  },
  [`&.${_listItemButtonClasses.default.focusVisible}`]: {
    backgroundColor: (theme.vars || theme).palette.action.focus
  },
  [`&.${_listItemButtonClasses.default.disabled}`]: {
    opacity: (theme.vars || theme).palette.action.disabledOpacity
  },
  variants: [{
    props: ({
      ownerState
    }) => ownerState.divider,
    style: {
      borderBottom: `1px solid ${(theme.vars || theme).palette.divider}`,
      backgroundClip: 'padding-box'
    }
  }, {
    props: {
      alignItems: 'flex-start'
    },
    style: {
      alignItems: 'flex-start'
    }
  }, {
    props: ({
      ownerState
    }) => !ownerState.disableGutters,
    style: {
      paddingLeft: 16,
      paddingRight: 16
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.dense,
    style: {
      paddingTop: 4,
      paddingBottom: 4
    }
  }]
})));
const ListItemButton = /*#__PURE__*/React.forwardRef(function ListItemButton(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiListItemButton'
  });
  const {
    alignItems = 'center',
    autoFocus = false,
    component = 'div',
    children,
    dense = false,
    disableGutters = false,
    divider = false,
    focusVisibleClassName,
    selected = false,
    className,
    ...other
  } = props;
  const context = React.useContext(_ListContext.default);
  const childContext = React.useMemo(() => ({
    dense: dense || context.dense || false,
    alignItems,
    disableGutters
  }), [alignItems, context.dense, dense, disableGutters]);
  const listItemRef = React.useRef(null);
  (0, _useEnhancedEffect.default)(() => {
    if (autoFocus) {
      if (listItemRef.current) {
        listItemRef.current.focus();
      } else if (process.env.NODE_ENV !== 'production') {
        console.error('MUI: Unable to set focus to a ListItemButton whose component has not been rendered.');
      }
    }
  }, [autoFocus]);
  const ownerState = {
    ...props,
    alignItems,
    dense: childContext.dense,
    disableGutters,
    divider,
    selected
  };
  const classes = useUtilityClasses(ownerState);
  const handleRef = (0, _useForkRef.default)(listItemRef, ref);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_ListContext.default.Provider, {
    value: childContext,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(ListItemButtonRoot, {
      ref: handleRef,
      href: other.href || other.to
      // `ButtonBase` processes `href` or `to` if `component` is set to 'button'
      ,
      component: (other.href || other.to) && component === 'div' ? 'button' : component,
      focusVisibleClassName: (0, _clsx.default)(classes.focusVisible, focusVisibleClassName),
      ownerState: ownerState,
      className: (0, _clsx.default)(classes.root, className),
      ...other,
      classes: classes,
      children: children
    })
  });
});
process.env.NODE_ENV !== "production" ? ListItemButton.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Defines the `align-items` style property.
   * @default 'center'
   */
  alignItems: _propTypes.default.oneOf(['center', 'flex-start']),
  /**
   * If `true`, the list item is focused during the first mount.
   * Focus will also be triggered if the value changes from false to true.
   * @default false
   */
  autoFocus: _propTypes.default.bool,
  /**
   * The content of the component if a `ListItemSecondaryAction` is used it must
   * be the last child.
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
   * If `true`, compact vertical padding designed for keyboard and mouse input is used.
   * The prop defaults to the value inherited from the parent List component.
   * @default false
   */
  dense: _propTypes.default.bool,
  /**
   * If `true`, the component is disabled.
   * @default false
   */
  disabled: _propTypes.default.bool,
  /**
   * If `true`, the left and right padding is removed.
   * @default false
   */
  disableGutters: _propTypes.default.bool,
  /**
   * If `true`, a 1px light border is added to the bottom of the list item.
   * @default false
   */
  divider: _propTypes.default.bool,
  /**
   * This prop can help identify which element has keyboard focus.
   * The class name will be applied when the element gains the focus through keyboard interaction.
   * It's a polyfill for the [CSS :focus-visible selector](https://drafts.csswg.org/selectors-4/#the-focus-visible-pseudo).
   * The rationale for using this feature [is explained here](https://github.com/WICG/focus-visible/blob/HEAD/explainer.md).
   * A [polyfill can be used](https://github.com/WICG/focus-visible) to apply a `focus-visible` class to other components
   * if needed.
   */
  focusVisibleClassName: _propTypes.default.string,
  /**
   * @ignore
   */
  href: _propTypes.default.string,
  /**
   * Use to apply selected styling.
   * @default false
   */
  selected: _propTypes.default.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = ListItemButton;