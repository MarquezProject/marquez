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
var _usePreviousProps = _interopRequireDefault(require("@mui/utils/usePreviousProps"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _useSlotProps = _interopRequireDefault(require("@mui/utils/useSlotProps"));
var _useBadge = _interopRequireDefault(require("./useBadge"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _createSimplePaletteValueFilter = _interopRequireDefault(require("../utils/createSimplePaletteValueFilter"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _badgeClasses = _interopRequireWildcard(require("./badgeClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const RADIUS_STANDARD = 10;
const RADIUS_DOT = 4;
const useUtilityClasses = ownerState => {
  const {
    color,
    anchorOrigin,
    invisible,
    overlap,
    variant,
    classes = {}
  } = ownerState;
  const slots = {
    root: ['root'],
    badge: ['badge', variant, invisible && 'invisible', `anchorOrigin${(0, _capitalize.default)(anchorOrigin.vertical)}${(0, _capitalize.default)(anchorOrigin.horizontal)}`, `anchorOrigin${(0, _capitalize.default)(anchorOrigin.vertical)}${(0, _capitalize.default)(anchorOrigin.horizontal)}${(0, _capitalize.default)(overlap)}`, `overlap${(0, _capitalize.default)(overlap)}`, color !== 'default' && `color${(0, _capitalize.default)(color)}`]
  };
  return (0, _composeClasses.default)(slots, _badgeClasses.getBadgeUtilityClass, classes);
};
const BadgeRoot = (0, _zeroStyled.styled)('span', {
  name: 'MuiBadge',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})({
  position: 'relative',
  display: 'inline-flex',
  // For correct alignment with the text.
  verticalAlign: 'middle',
  flexShrink: 0
});
const BadgeBadge = (0, _zeroStyled.styled)('span', {
  name: 'MuiBadge',
  slot: 'Badge',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.badge, styles[ownerState.variant], styles[`anchorOrigin${(0, _capitalize.default)(ownerState.anchorOrigin.vertical)}${(0, _capitalize.default)(ownerState.anchorOrigin.horizontal)}${(0, _capitalize.default)(ownerState.overlap)}`], ownerState.color !== 'default' && styles[`color${(0, _capitalize.default)(ownerState.color)}`], ownerState.invisible && styles.invisible];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'flex',
  flexDirection: 'row',
  flexWrap: 'wrap',
  justifyContent: 'center',
  alignContent: 'center',
  alignItems: 'center',
  position: 'absolute',
  boxSizing: 'border-box',
  fontFamily: theme.typography.fontFamily,
  fontWeight: theme.typography.fontWeightMedium,
  fontSize: theme.typography.pxToRem(12),
  minWidth: RADIUS_STANDARD * 2,
  lineHeight: 1,
  padding: '0 6px',
  height: RADIUS_STANDARD * 2,
  borderRadius: RADIUS_STANDARD,
  zIndex: 1,
  // Render the badge on top of potential ripples.
  transition: theme.transitions.create('transform', {
    easing: theme.transitions.easing.easeInOut,
    duration: theme.transitions.duration.enteringScreen
  }),
  variants: [...Object.entries(theme.palette).filter((0, _createSimplePaletteValueFilter.default)(['contrastText'])).map(([color]) => ({
    props: {
      color
    },
    style: {
      backgroundColor: (theme.vars || theme).palette[color].main,
      color: (theme.vars || theme).palette[color].contrastText
    }
  })), {
    props: {
      variant: 'dot'
    },
    style: {
      borderRadius: RADIUS_DOT,
      height: RADIUS_DOT * 2,
      minWidth: RADIUS_DOT * 2,
      padding: 0
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.vertical === 'top' && ownerState.anchorOrigin.horizontal === 'right' && ownerState.overlap === 'rectangular',
    style: {
      top: 0,
      right: 0,
      transform: 'scale(1) translate(50%, -50%)',
      transformOrigin: '100% 0%',
      [`&.${_badgeClasses.default.invisible}`]: {
        transform: 'scale(0) translate(50%, -50%)'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.vertical === 'bottom' && ownerState.anchorOrigin.horizontal === 'right' && ownerState.overlap === 'rectangular',
    style: {
      bottom: 0,
      right: 0,
      transform: 'scale(1) translate(50%, 50%)',
      transformOrigin: '100% 100%',
      [`&.${_badgeClasses.default.invisible}`]: {
        transform: 'scale(0) translate(50%, 50%)'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.vertical === 'top' && ownerState.anchorOrigin.horizontal === 'left' && ownerState.overlap === 'rectangular',
    style: {
      top: 0,
      left: 0,
      transform: 'scale(1) translate(-50%, -50%)',
      transformOrigin: '0% 0%',
      [`&.${_badgeClasses.default.invisible}`]: {
        transform: 'scale(0) translate(-50%, -50%)'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.vertical === 'bottom' && ownerState.anchorOrigin.horizontal === 'left' && ownerState.overlap === 'rectangular',
    style: {
      bottom: 0,
      left: 0,
      transform: 'scale(1) translate(-50%, 50%)',
      transformOrigin: '0% 100%',
      [`&.${_badgeClasses.default.invisible}`]: {
        transform: 'scale(0) translate(-50%, 50%)'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.vertical === 'top' && ownerState.anchorOrigin.horizontal === 'right' && ownerState.overlap === 'circular',
    style: {
      top: '14%',
      right: '14%',
      transform: 'scale(1) translate(50%, -50%)',
      transformOrigin: '100% 0%',
      [`&.${_badgeClasses.default.invisible}`]: {
        transform: 'scale(0) translate(50%, -50%)'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.vertical === 'bottom' && ownerState.anchorOrigin.horizontal === 'right' && ownerState.overlap === 'circular',
    style: {
      bottom: '14%',
      right: '14%',
      transform: 'scale(1) translate(50%, 50%)',
      transformOrigin: '100% 100%',
      [`&.${_badgeClasses.default.invisible}`]: {
        transform: 'scale(0) translate(50%, 50%)'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.vertical === 'top' && ownerState.anchorOrigin.horizontal === 'left' && ownerState.overlap === 'circular',
    style: {
      top: '14%',
      left: '14%',
      transform: 'scale(1) translate(-50%, -50%)',
      transformOrigin: '0% 0%',
      [`&.${_badgeClasses.default.invisible}`]: {
        transform: 'scale(0) translate(-50%, -50%)'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.anchorOrigin.vertical === 'bottom' && ownerState.anchorOrigin.horizontal === 'left' && ownerState.overlap === 'circular',
    style: {
      bottom: '14%',
      left: '14%',
      transform: 'scale(1) translate(-50%, 50%)',
      transformOrigin: '0% 100%',
      [`&.${_badgeClasses.default.invisible}`]: {
        transform: 'scale(0) translate(-50%, 50%)'
      }
    }
  }, {
    props: {
      invisible: true
    },
    style: {
      transition: theme.transitions.create('transform', {
        easing: theme.transitions.easing.easeInOut,
        duration: theme.transitions.duration.leavingScreen
      })
    }
  }]
})));
function getAnchorOrigin(anchorOrigin) {
  return {
    vertical: anchorOrigin?.vertical ?? 'top',
    horizontal: anchorOrigin?.horizontal ?? 'right'
  };
}
const Badge = /*#__PURE__*/React.forwardRef(function Badge(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiBadge'
  });
  const {
    anchorOrigin: anchorOriginProp,
    className,
    classes: classesProp,
    component,
    components = {},
    componentsProps = {},
    children,
    overlap: overlapProp = 'rectangular',
    color: colorProp = 'default',
    invisible: invisibleProp = false,
    max: maxProp = 99,
    badgeContent: badgeContentProp,
    slots,
    slotProps,
    showZero = false,
    variant: variantProp = 'standard',
    ...other
  } = props;
  const {
    badgeContent,
    invisible: invisibleFromHook,
    max,
    displayValue: displayValueFromHook
  } = (0, _useBadge.default)({
    max: maxProp,
    invisible: invisibleProp,
    badgeContent: badgeContentProp,
    showZero
  });
  const prevProps = (0, _usePreviousProps.default)({
    anchorOrigin: getAnchorOrigin(anchorOriginProp),
    color: colorProp,
    overlap: overlapProp,
    variant: variantProp,
    badgeContent: badgeContentProp
  });
  const invisible = invisibleFromHook || badgeContent == null && variantProp !== 'dot';
  const {
    color = colorProp,
    overlap = overlapProp,
    anchorOrigin: anchorOriginPropProp,
    variant = variantProp
  } = invisible ? prevProps : props;
  const anchorOrigin = getAnchorOrigin(anchorOriginPropProp);
  const displayValue = variant !== 'dot' ? displayValueFromHook : undefined;
  const ownerState = {
    ...props,
    badgeContent,
    invisible,
    max,
    displayValue,
    showZero,
    anchorOrigin,
    color,
    overlap,
    variant
  };
  const classes = useUtilityClasses(ownerState);

  // support both `slots` and `components` for backward compatibility
  const RootSlot = slots?.root ?? components.Root ?? BadgeRoot;
  const BadgeSlot = slots?.badge ?? components.Badge ?? BadgeBadge;
  const rootSlotProps = slotProps?.root ?? componentsProps.root;
  const badgeSlotProps = slotProps?.badge ?? componentsProps.badge;
  const rootProps = (0, _useSlotProps.default)({
    elementType: RootSlot,
    externalSlotProps: rootSlotProps,
    externalForwardedProps: other,
    additionalProps: {
      ref,
      as: component
    },
    ownerState,
    className: (0, _clsx.default)(rootSlotProps?.className, classes.root, className)
  });
  const badgeProps = (0, _useSlotProps.default)({
    elementType: BadgeSlot,
    externalSlotProps: badgeSlotProps,
    ownerState,
    className: (0, _clsx.default)(classes.badge, badgeSlotProps?.className)
  });
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(RootSlot, {
    ...rootProps,
    children: [children, /*#__PURE__*/(0, _jsxRuntime.jsx)(BadgeSlot, {
      ...badgeProps,
      children: displayValue
    })]
  });
});
process.env.NODE_ENV !== "production" ? Badge.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The anchor of the badge.
   * @default {
   *   vertical: 'top',
   *   horizontal: 'right',
   * }
   */
  anchorOrigin: _propTypes.default.shape({
    horizontal: _propTypes.default.oneOf(['left', 'right']),
    vertical: _propTypes.default.oneOf(['bottom', 'top'])
  }),
  /**
   * The content rendered within the badge.
   */
  badgeContent: _propTypes.default.node,
  /**
   * The badge will be added relative to this node.
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
   * The color of the component.
   * It supports both default and custom theme colors, which can be added as shown in the
   * [palette customization guide](https://mui.com/material-ui/customization/palette/#custom-colors).
   * @default 'default'
   */
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['default', 'primary', 'secondary', 'error', 'info', 'success', 'warning']), _propTypes.default.string]),
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * The components used for each slot inside.
   *
   * @deprecated use the `slots` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   *
   * @default {}
   */
  components: _propTypes.default.shape({
    Badge: _propTypes.default.elementType,
    Root: _propTypes.default.elementType
  }),
  /**
   * The extra props for the slot components.
   * You can override the existing props or add new ones.
   *
   * @deprecated use the `slotProps` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   *
   * @default {}
   */
  componentsProps: _propTypes.default.shape({
    badge: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    root: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * If `true`, the badge is invisible.
   * @default false
   */
  invisible: _propTypes.default.bool,
  /**
   * Max count to show.
   * @default 99
   */
  max: _propTypes.default.number,
  /**
   * Wrapped shape the badge should overlap.
   * @default 'rectangular'
   */
  overlap: _propTypes.default.oneOf(['circular', 'rectangular']),
  /**
   * Controls whether the badge is hidden when `badgeContent` is zero.
   * @default false
   */
  showZero: _propTypes.default.bool,
  /**
   * The props used for each slot inside the Badge.
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    badge: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    root: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * The components used for each slot inside the Badge.
   * Either a string to use a HTML element or a component.
   * @default {}
   */
  slots: _propTypes.default.shape({
    badge: _propTypes.default.elementType,
    root: _propTypes.default.elementType
  }),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The variant to use.
   * @default 'standard'
   */
  variant: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['dot', 'standard']), _propTypes.default.string])
} : void 0;
var _default = exports.default = Badge;