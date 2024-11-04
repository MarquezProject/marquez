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
var _colorManipulator = require("@mui/system/colorManipulator");
var _RtlProvider = require("@mui/system/RtlProvider");
var _paginationItemClasses = _interopRequireWildcard(require("./paginationItemClasses"));
var _ButtonBase = _interopRequireDefault(require("../ButtonBase"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _createSimplePaletteValueFilter = _interopRequireDefault(require("../utils/createSimplePaletteValueFilter"));
var _FirstPage = _interopRequireDefault(require("../internal/svg-icons/FirstPage"));
var _LastPage = _interopRequireDefault(require("../internal/svg-icons/LastPage"));
var _NavigateBefore = _interopRequireDefault(require("../internal/svg-icons/NavigateBefore"));
var _NavigateNext = _interopRequireDefault(require("../internal/svg-icons/NavigateNext"));
var _useSlot = _interopRequireDefault(require("../utils/useSlot"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _jsxRuntime = require("react/jsx-runtime");
const overridesResolver = (props, styles) => {
  const {
    ownerState
  } = props;
  return [styles.root, styles[ownerState.variant], styles[`size${(0, _capitalize.default)(ownerState.size)}`], ownerState.variant === 'text' && styles[`text${(0, _capitalize.default)(ownerState.color)}`], ownerState.variant === 'outlined' && styles[`outlined${(0, _capitalize.default)(ownerState.color)}`], ownerState.shape === 'rounded' && styles.rounded, ownerState.type === 'page' && styles.page, (ownerState.type === 'start-ellipsis' || ownerState.type === 'end-ellipsis') && styles.ellipsis, (ownerState.type === 'previous' || ownerState.type === 'next') && styles.previousNext, (ownerState.type === 'first' || ownerState.type === 'last') && styles.firstLast];
};
const useUtilityClasses = ownerState => {
  const {
    classes,
    color,
    disabled,
    selected,
    size,
    shape,
    type,
    variant
  } = ownerState;
  const slots = {
    root: ['root', `size${(0, _capitalize.default)(size)}`, variant, shape, color !== 'standard' && `color${(0, _capitalize.default)(color)}`, color !== 'standard' && `${variant}${(0, _capitalize.default)(color)}`, disabled && 'disabled', selected && 'selected', {
      page: 'page',
      first: 'firstLast',
      last: 'firstLast',
      'start-ellipsis': 'ellipsis',
      'end-ellipsis': 'ellipsis',
      previous: 'previousNext',
      next: 'previousNext'
    }[type]],
    icon: ['icon']
  };
  return (0, _composeClasses.default)(slots, _paginationItemClasses.getPaginationItemUtilityClass, classes);
};
const PaginationItemEllipsis = (0, _zeroStyled.styled)('div', {
  name: 'MuiPaginationItem',
  slot: 'Root',
  overridesResolver
})((0, _memoTheme.default)(({
  theme
}) => ({
  ...theme.typography.body2,
  borderRadius: 32 / 2,
  textAlign: 'center',
  boxSizing: 'border-box',
  minWidth: 32,
  padding: '0 6px',
  margin: '0 3px',
  color: (theme.vars || theme).palette.text.primary,
  height: 'auto',
  [`&.${_paginationItemClasses.default.disabled}`]: {
    opacity: (theme.vars || theme).palette.action.disabledOpacity
  },
  variants: [{
    props: {
      size: 'small'
    },
    style: {
      minWidth: 26,
      borderRadius: 26 / 2,
      margin: '0 1px',
      padding: '0 4px'
    }
  }, {
    props: {
      size: 'large'
    },
    style: {
      minWidth: 40,
      borderRadius: 40 / 2,
      padding: '0 10px',
      fontSize: theme.typography.pxToRem(15)
    }
  }]
})));
const PaginationItemPage = (0, _zeroStyled.styled)(_ButtonBase.default, {
  name: 'MuiPaginationItem',
  slot: 'Root',
  overridesResolver
})((0, _memoTheme.default)(({
  theme
}) => ({
  ...theme.typography.body2,
  borderRadius: 32 / 2,
  textAlign: 'center',
  boxSizing: 'border-box',
  minWidth: 32,
  height: 32,
  padding: '0 6px',
  margin: '0 3px',
  color: (theme.vars || theme).palette.text.primary,
  [`&.${_paginationItemClasses.default.focusVisible}`]: {
    backgroundColor: (theme.vars || theme).palette.action.focus
  },
  [`&.${_paginationItemClasses.default.disabled}`]: {
    opacity: (theme.vars || theme).palette.action.disabledOpacity
  },
  transition: theme.transitions.create(['color', 'background-color'], {
    duration: theme.transitions.duration.short
  }),
  '&:hover': {
    backgroundColor: (theme.vars || theme).palette.action.hover,
    // Reset on touch devices, it doesn't add specificity
    '@media (hover: none)': {
      backgroundColor: 'transparent'
    }
  },
  [`&.${_paginationItemClasses.default.selected}`]: {
    backgroundColor: (theme.vars || theme).palette.action.selected,
    '&:hover': {
      backgroundColor: theme.vars ? `rgba(${theme.vars.palette.action.selectedChannel} / calc(${theme.vars.palette.action.selectedOpacity} + ${theme.vars.palette.action.hoverOpacity}))` : (0, _colorManipulator.alpha)(theme.palette.action.selected, theme.palette.action.selectedOpacity + theme.palette.action.hoverOpacity),
      // Reset on touch devices, it doesn't add specificity
      '@media (hover: none)': {
        backgroundColor: (theme.vars || theme).palette.action.selected
      }
    },
    [`&.${_paginationItemClasses.default.focusVisible}`]: {
      backgroundColor: theme.vars ? `rgba(${theme.vars.palette.action.selectedChannel} / calc(${theme.vars.palette.action.selectedOpacity} + ${theme.vars.palette.action.focusOpacity}))` : (0, _colorManipulator.alpha)(theme.palette.action.selected, theme.palette.action.selectedOpacity + theme.palette.action.focusOpacity)
    },
    [`&.${_paginationItemClasses.default.disabled}`]: {
      opacity: 1,
      color: (theme.vars || theme).palette.action.disabled,
      backgroundColor: (theme.vars || theme).palette.action.selected
    }
  },
  variants: [{
    props: {
      size: 'small'
    },
    style: {
      minWidth: 26,
      height: 26,
      borderRadius: 26 / 2,
      margin: '0 1px',
      padding: '0 4px'
    }
  }, {
    props: {
      size: 'large'
    },
    style: {
      minWidth: 40,
      height: 40,
      borderRadius: 40 / 2,
      padding: '0 10px',
      fontSize: theme.typography.pxToRem(15)
    }
  }, {
    props: {
      shape: 'rounded'
    },
    style: {
      borderRadius: (theme.vars || theme).shape.borderRadius
    }
  }, {
    props: {
      variant: 'outlined'
    },
    style: {
      border: theme.vars ? `1px solid rgba(${theme.vars.palette.common.onBackgroundChannel} / 0.23)` : `1px solid ${theme.palette.mode === 'light' ? 'rgba(0, 0, 0, 0.23)' : 'rgba(255, 255, 255, 0.23)'}`,
      [`&.${_paginationItemClasses.default.selected}`]: {
        [`&.${_paginationItemClasses.default.disabled}`]: {
          borderColor: (theme.vars || theme).palette.action.disabledBackground,
          color: (theme.vars || theme).palette.action.disabled
        }
      }
    }
  }, {
    props: {
      variant: 'text'
    },
    style: {
      [`&.${_paginationItemClasses.default.selected}`]: {
        [`&.${_paginationItemClasses.default.disabled}`]: {
          color: (theme.vars || theme).palette.action.disabled
        }
      }
    }
  }, ...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)(['dark', 'contrastText'])).map(([color]) => ({
    props: {
      variant: 'text',
      color
    },
    style: {
      [`&.${_paginationItemClasses.default.selected}`]: {
        color: (theme.vars || theme).palette[color].contrastText,
        backgroundColor: (theme.vars || theme).palette[color].main,
        '&:hover': {
          backgroundColor: (theme.vars || theme).palette[color].dark,
          // Reset on touch devices, it doesn't add specificity
          '@media (hover: none)': {
            backgroundColor: (theme.vars || theme).palette[color].main
          }
        },
        [`&.${_paginationItemClasses.default.focusVisible}`]: {
          backgroundColor: (theme.vars || theme).palette[color].dark
        },
        [`&.${_paginationItemClasses.default.disabled}`]: {
          color: (theme.vars || theme).palette.action.disabled
        }
      }
    }
  })), ...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)(['light'])).map(([color]) => ({
    props: {
      variant: 'outlined',
      color
    },
    style: {
      [`&.${_paginationItemClasses.default.selected}`]: {
        color: (theme.vars || theme).palette[color].main,
        border: `1px solid ${theme.vars ? `rgba(${theme.vars.palette[color].mainChannel} / 0.5)` : (0, _colorManipulator.alpha)(theme.palette[color].main, 0.5)}`,
        backgroundColor: theme.vars ? `rgba(${theme.vars.palette[color].mainChannel} / ${theme.vars.palette.action.activatedOpacity})` : (0, _colorManipulator.alpha)(theme.palette[color].main, theme.palette.action.activatedOpacity),
        '&:hover': {
          backgroundColor: theme.vars ? `rgba(${theme.vars.palette[color].mainChannel} / calc(${theme.vars.palette.action.activatedOpacity} + ${theme.vars.palette.action.focusOpacity}))` : (0, _colorManipulator.alpha)(theme.palette[color].main, theme.palette.action.activatedOpacity + theme.palette.action.focusOpacity),
          // Reset on touch devices, it doesn't add specificity
          '@media (hover: none)': {
            backgroundColor: 'transparent'
          }
        },
        [`&.${_paginationItemClasses.default.focusVisible}`]: {
          backgroundColor: theme.vars ? `rgba(${theme.vars.palette[color].mainChannel} / calc(${theme.vars.palette.action.activatedOpacity} + ${theme.vars.palette.action.focusOpacity}))` : (0, _colorManipulator.alpha)(theme.palette[color].main, theme.palette.action.activatedOpacity + theme.palette.action.focusOpacity)
        }
      }
    }
  }))]
})));
const PaginationItemPageIcon = (0, _zeroStyled.styled)('div', {
  name: 'MuiPaginationItem',
  slot: 'Icon',
  overridesResolver: (props, styles) => styles.icon
})((0, _memoTheme.default)(({
  theme
}) => ({
  fontSize: theme.typography.pxToRem(20),
  margin: '0 -8px',
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
      fontSize: theme.typography.pxToRem(22)
    }
  }]
})));
const PaginationItem = /*#__PURE__*/React.forwardRef(function PaginationItem(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiPaginationItem'
  });
  const {
    className,
    color = 'standard',
    component,
    components = {},
    disabled = false,
    page,
    selected = false,
    shape = 'circular',
    size = 'medium',
    slots = {},
    slotProps = {},
    type = 'page',
    variant = 'text',
    ...other
  } = props;
  const ownerState = {
    ...props,
    color,
    disabled,
    selected,
    shape,
    size,
    type,
    variant
  };
  const isRtl = (0, _RtlProvider.useRtl)();
  const classes = useUtilityClasses(ownerState);
  const externalForwardedProps = {
    slots: {
      previous: slots.previous ?? components.previous,
      next: slots.next ?? components.next,
      first: slots.first ?? components.first,
      last: slots.last ?? components.last
    },
    slotProps
  };
  const [PreviousSlot, previousSlotProps] = (0, _useSlot.default)('previous', {
    elementType: _NavigateBefore.default,
    externalForwardedProps,
    ownerState
  });
  const [NextSlot, nextSlotProps] = (0, _useSlot.default)('next', {
    elementType: _NavigateNext.default,
    externalForwardedProps,
    ownerState
  });
  const [FirstSlot, firstSlotProps] = (0, _useSlot.default)('first', {
    elementType: _FirstPage.default,
    externalForwardedProps,
    ownerState
  });
  const [LastSlot, lastSlotProps] = (0, _useSlot.default)('last', {
    elementType: _LastPage.default,
    externalForwardedProps,
    ownerState
  });
  const rtlAwareType = isRtl ? {
    previous: 'next',
    next: 'previous',
    first: 'last',
    last: 'first'
  }[type] : type;
  const IconSlot = {
    previous: PreviousSlot,
    next: NextSlot,
    first: FirstSlot,
    last: LastSlot
  }[rtlAwareType];
  const iconSlotProps = {
    previous: previousSlotProps,
    next: nextSlotProps,
    first: firstSlotProps,
    last: lastSlotProps
  }[rtlAwareType];
  return type === 'start-ellipsis' || type === 'end-ellipsis' ? /*#__PURE__*/(0, _jsxRuntime.jsx)(PaginationItemEllipsis, {
    ref: ref,
    ownerState: ownerState,
    className: (0, _clsx.default)(classes.root, className),
    children: "\u2026"
  }) : /*#__PURE__*/(0, _jsxRuntime.jsxs)(PaginationItemPage, {
    ref: ref,
    ownerState: ownerState,
    component: component,
    disabled: disabled,
    className: (0, _clsx.default)(classes.root, className),
    ...other,
    children: [type === 'page' && page, IconSlot ? /*#__PURE__*/(0, _jsxRuntime.jsx)(PaginationItemPageIcon, {
      ...iconSlotProps,
      className: classes.icon,
      as: IconSlot
    }) : null]
  });
});
process.env.NODE_ENV !== "production" ? PaginationItem.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * @ignore
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
   * The active color.
   * It supports both default and custom theme colors, which can be added as shown in the
   * [palette customization guide](https://mui.com/material-ui/customization/palette/#custom-colors).
   * @default 'standard'
   */
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['primary', 'secondary', 'standard']), _propTypes.default.string]),
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * The components used for each slot inside.
   *
   * This prop is an alias for the `slots` prop.
   * It's recommended to use the `slots` prop instead.
   *
   * @default {}
   * @deprecated use the `slots` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   */
  components: _propTypes.default.shape({
    first: _propTypes.default.elementType,
    last: _propTypes.default.elementType,
    next: _propTypes.default.elementType,
    previous: _propTypes.default.elementType
  }),
  /**
   * If `true`, the component is disabled.
   * @default false
   */
  disabled: _propTypes.default.bool,
  /**
   * The current page number.
   */
  page: _propTypes.default.node,
  /**
   * If `true` the pagination item is selected.
   * @default false
   */
  selected: _propTypes.default.bool,
  /**
   * The shape of the pagination item.
   * @default 'circular'
   */
  shape: _propTypes.default.oneOf(['circular', 'rounded']),
  /**
   * The size of the component.
   * @default 'medium'
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['small', 'medium', 'large']), _propTypes.default.string]),
  /**
   * The props used for each slot inside.
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    first: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    last: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    next: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    previous: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * The components used for each slot inside.
   * @default {}
   */
  slots: _propTypes.default.shape({
    first: _propTypes.default.elementType,
    last: _propTypes.default.elementType,
    next: _propTypes.default.elementType,
    previous: _propTypes.default.elementType
  }),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The type of pagination item.
   * @default 'page'
   */
  type: _propTypes.default.oneOf(['end-ellipsis', 'first', 'last', 'next', 'page', 'previous', 'start-ellipsis']),
  /**
   * The variant to use.
   * @default 'text'
   */
  variant: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['outlined', 'text']), _propTypes.default.string])
} : void 0;
var _default = exports.default = PaginationItem;