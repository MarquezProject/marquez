"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.overridesResolver = exports.default = exports.ListItemRoot = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _elementTypeAcceptingRef = _interopRequireDefault(require("@mui/utils/elementTypeAcceptingRef"));
var _chainPropTypes = _interopRequireDefault(require("@mui/utils/chainPropTypes"));
var _isHostComponent = _interopRequireDefault(require("../utils/isHostComponent"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _isMuiElement = _interopRequireDefault(require("../utils/isMuiElement"));
var _useForkRef = _interopRequireDefault(require("../utils/useForkRef"));
var _ListContext = _interopRequireDefault(require("../List/ListContext"));
var _listItemClasses = require("./listItemClasses");
var _ListItemButton = require("../ListItemButton");
var _ListItemSecondaryAction = _interopRequireDefault(require("../ListItemSecondaryAction"));
var _jsxRuntime = require("react/jsx-runtime");
const overridesResolver = (props, styles) => {
  const {
    ownerState
  } = props;
  return [styles.root, ownerState.dense && styles.dense, ownerState.alignItems === 'flex-start' && styles.alignItemsFlexStart, ownerState.divider && styles.divider, !ownerState.disableGutters && styles.gutters, !ownerState.disablePadding && styles.padding, ownerState.hasSecondaryAction && styles.secondaryAction];
};
exports.overridesResolver = overridesResolver;
const useUtilityClasses = ownerState => {
  const {
    alignItems,
    classes,
    dense,
    disableGutters,
    disablePadding,
    divider,
    hasSecondaryAction
  } = ownerState;
  const slots = {
    root: ['root', dense && 'dense', !disableGutters && 'gutters', !disablePadding && 'padding', divider && 'divider', alignItems === 'flex-start' && 'alignItemsFlexStart', hasSecondaryAction && 'secondaryAction'],
    container: ['container']
  };
  return (0, _composeClasses.default)(slots, _listItemClasses.getListItemUtilityClass, classes);
};
const ListItemRoot = exports.ListItemRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiListItem',
  slot: 'Root',
  overridesResolver
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'flex',
  justifyContent: 'flex-start',
  alignItems: 'center',
  position: 'relative',
  textDecoration: 'none',
  width: '100%',
  boxSizing: 'border-box',
  textAlign: 'left',
  variants: [{
    props: ({
      ownerState
    }) => !ownerState.disablePadding,
    style: {
      paddingTop: 8,
      paddingBottom: 8
    }
  }, {
    props: ({
      ownerState
    }) => !ownerState.disablePadding && ownerState.dense,
    style: {
      paddingTop: 4,
      paddingBottom: 4
    }
  }, {
    props: ({
      ownerState
    }) => !ownerState.disablePadding && !ownerState.disableGutters,
    style: {
      paddingLeft: 16,
      paddingRight: 16
    }
  }, {
    props: ({
      ownerState
    }) => !ownerState.disablePadding && !!ownerState.secondaryAction,
    style: {
      // Add some space to avoid collision as `ListItemSecondaryAction`
      // is absolutely positioned.
      paddingRight: 48
    }
  }, {
    props: ({
      ownerState
    }) => !!ownerState.secondaryAction,
    style: {
      [`& > .${_ListItemButton.listItemButtonClasses.root}`]: {
        paddingRight: 48
      }
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
    }) => ownerState.divider,
    style: {
      borderBottom: `1px solid ${(theme.vars || theme).palette.divider}`,
      backgroundClip: 'padding-box'
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.button,
    style: {
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
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.hasSecondaryAction,
    style: {
      // Add some space to avoid collision as `ListItemSecondaryAction`
      // is absolutely positioned.
      paddingRight: 48
    }
  }]
})));
const ListItemContainer = (0, _zeroStyled.styled)('li', {
  name: 'MuiListItem',
  slot: 'Container',
  overridesResolver: (props, styles) => styles.container
})({
  position: 'relative'
});

/**
 * Uses an additional container component if `ListItemSecondaryAction` is the last child.
 */
const ListItem = /*#__PURE__*/React.forwardRef(function ListItem(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiListItem'
  });
  const {
    alignItems = 'center',
    children: childrenProp,
    className,
    component: componentProp,
    components = {},
    componentsProps = {},
    ContainerComponent = 'li',
    ContainerProps: {
      className: ContainerClassName,
      ...ContainerProps
    } = {},
    dense = false,
    disableGutters = false,
    disablePadding = false,
    divider = false,
    secondaryAction,
    slotProps = {},
    slots = {},
    ...other
  } = props;
  const context = React.useContext(_ListContext.default);
  const childContext = React.useMemo(() => ({
    dense: dense || context.dense || false,
    alignItems,
    disableGutters
  }), [alignItems, context.dense, dense, disableGutters]);
  const listItemRef = React.useRef(null);
  const children = React.Children.toArray(childrenProp);

  // v4 implementation, deprecated in v6, will be removed in v7
  const hasSecondaryAction = children.length && (0, _isMuiElement.default)(children[children.length - 1], ['ListItemSecondaryAction']);
  const ownerState = {
    ...props,
    alignItems,
    dense: childContext.dense,
    disableGutters,
    disablePadding,
    divider,
    hasSecondaryAction
  };
  const classes = useUtilityClasses(ownerState);
  const handleRef = (0, _useForkRef.default)(listItemRef, ref);
  const Root = slots.root || components.Root || ListItemRoot;
  const rootProps = slotProps.root || componentsProps.root || {};
  const componentProps = {
    className: (0, _clsx.default)(classes.root, rootProps.className, className),
    ...other
  };
  let Component = componentProp || 'li';

  // v4 implementation, deprecated in v6, will be removed in v7
  if (hasSecondaryAction) {
    // Use div by default.
    Component = !componentProps.component && !componentProp ? 'div' : Component;

    // Avoid nesting of li > li.
    if (ContainerComponent === 'li') {
      if (Component === 'li') {
        Component = 'div';
      } else if (componentProps.component === 'li') {
        componentProps.component = 'div';
      }
    }
    return /*#__PURE__*/(0, _jsxRuntime.jsx)(_ListContext.default.Provider, {
      value: childContext,
      children: /*#__PURE__*/(0, _jsxRuntime.jsxs)(ListItemContainer, {
        as: ContainerComponent,
        className: (0, _clsx.default)(classes.container, ContainerClassName),
        ref: handleRef,
        ownerState: ownerState,
        ...ContainerProps,
        children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(Root, {
          ...rootProps,
          ...(!(0, _isHostComponent.default)(Root) && {
            as: Component,
            ownerState: {
              ...ownerState,
              ...rootProps.ownerState
            }
          }),
          ...componentProps,
          children: children
        }), children.pop()]
      })
    });
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_ListContext.default.Provider, {
    value: childContext,
    children: /*#__PURE__*/(0, _jsxRuntime.jsxs)(Root, {
      ...rootProps,
      as: Component,
      ref: handleRef,
      ...(!(0, _isHostComponent.default)(Root) && {
        ownerState: {
          ...ownerState,
          ...rootProps.ownerState
        }
      }),
      ...componentProps,
      children: [children, secondaryAction && /*#__PURE__*/(0, _jsxRuntime.jsx)(_ListItemSecondaryAction.default, {
        children: secondaryAction
      })]
    })
  });
});
process.env.NODE_ENV !== "production" ? ListItem.propTypes /* remove-proptypes */ = {
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
   * The content of the component if a `ListItemSecondaryAction` is used it must
   * be the last child.
   */
  children: (0, _chainPropTypes.default)(_propTypes.default.node, props => {
    const children = React.Children.toArray(props.children);

    // React.Children.toArray(props.children).findLastIndex(isListItemSecondaryAction)
    let secondaryActionIndex = -1;
    for (let i = children.length - 1; i >= 0; i -= 1) {
      const child = children[i];
      if ((0, _isMuiElement.default)(child, ['ListItemSecondaryAction'])) {
        secondaryActionIndex = i;
        break;
      }
    }

    //  is ListItemSecondaryAction the last child of ListItem
    if (secondaryActionIndex !== -1 && secondaryActionIndex !== children.length - 1) {
      return new Error('MUI: You used an element after ListItemSecondaryAction. ' + 'For ListItem to detect that it has a secondary action ' + 'you must pass it as the last child to ListItem.');
    }
    return null;
  }),
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
   * The components used for each slot inside.
   *
   * @deprecated Use the `slots` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   * @default {}
   */
  components: _propTypes.default.shape({
    Root: _propTypes.default.elementType
  }),
  /**
   * The extra props for the slot components.
   * You can override the existing props or add new ones.
   *
   * @deprecated Use the `slotProps` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   * @default {}
   */
  componentsProps: _propTypes.default.shape({
    root: _propTypes.default.object
  }),
  /**
   * The container component used when a `ListItemSecondaryAction` is the last child.
   * @default 'li'
   * @deprecated Use the `component` or `slots.root` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   */
  ContainerComponent: _elementTypeAcceptingRef.default,
  /**
   * Props applied to the container component if used.
   * @default {}
   * @deprecated Use the `slotProps.root` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   */
  ContainerProps: _propTypes.default.object,
  /**
   * If `true`, compact vertical padding designed for keyboard and mouse input is used.
   * The prop defaults to the value inherited from the parent List component.
   * @default false
   */
  dense: _propTypes.default.bool,
  /**
   * If `true`, the left and right padding is removed.
   * @default false
   */
  disableGutters: _propTypes.default.bool,
  /**
   * If `true`, all padding is removed.
   * @default false
   */
  disablePadding: _propTypes.default.bool,
  /**
   * If `true`, a 1px light border is added to the bottom of the list item.
   * @default false
   */
  divider: _propTypes.default.bool,
  /**
   * The element to display at the end of ListItem.
   */
  secondaryAction: _propTypes.default.node,
  /**
   * The extra props for the slot components.
   * You can override the existing props or add new ones.
   *
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    root: _propTypes.default.object
  }),
  /**
   * The components used for each slot inside.
   *
   * @default {}
   */
  slots: _propTypes.default.shape({
    root: _propTypes.default.elementType
  }),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = ListItem;