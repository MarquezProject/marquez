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
var _reactIs = require("react-is");
var _clsx = _interopRequireDefault(require("clsx"));
var _chainPropTypes = _interopRequireDefault(require("@mui/utils/chainPropTypes"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _Avatar = _interopRequireWildcard(require("../Avatar"));
var _avatarGroupClasses = _interopRequireWildcard(require("./avatarGroupClasses"));
var _useSlot = _interopRequireDefault(require("../utils/useSlot"));
var _jsxRuntime = require("react/jsx-runtime");
const SPACINGS = {
  small: -16,
  medium: -8
};
const useUtilityClasses = ownerState => {
  const {
    classes
  } = ownerState;
  const slots = {
    root: ['root'],
    avatar: ['avatar']
  };
  return (0, _composeClasses.default)(slots, _avatarGroupClasses.getAvatarGroupUtilityClass, classes);
};
const AvatarGroupRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiAvatarGroup',
  slot: 'Root',
  overridesResolver: (props, styles) => ({
    [`& .${_avatarGroupClasses.default.avatar}`]: styles.avatar,
    ...styles.root
  })
})((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'flex',
  flexDirection: 'row-reverse',
  [`& .${_Avatar.avatarClasses.root}`]: {
    border: `2px solid ${(theme.vars || theme).palette.background.default}`,
    boxSizing: 'content-box',
    marginLeft: 'var(--AvatarGroup-spacing, -8px)',
    '&:last-child': {
      marginLeft: 0
    }
  }
})));
const AvatarGroup = /*#__PURE__*/React.forwardRef(function AvatarGroup(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiAvatarGroup'
  });
  const {
    children: childrenProp,
    className,
    component = 'div',
    componentsProps,
    max = 5,
    renderSurplus,
    slotProps = {},
    slots = {},
    spacing = 'medium',
    total,
    variant = 'circular',
    ...other
  } = props;
  let clampedMax = max < 2 ? 2 : max;
  const ownerState = {
    ...props,
    max,
    spacing,
    component,
    variant
  };
  const classes = useUtilityClasses(ownerState);
  const children = React.Children.toArray(childrenProp).filter(child => {
    if (process.env.NODE_ENV !== 'production') {
      if ((0, _reactIs.isFragment)(child)) {
        console.error(["MUI: The AvatarGroup component doesn't accept a Fragment as a child.", 'Consider providing an array instead.'].join('\n'));
      }
    }
    return /*#__PURE__*/React.isValidElement(child);
  });
  const totalAvatars = total || children.length;
  if (totalAvatars === clampedMax) {
    clampedMax += 1;
  }
  clampedMax = Math.min(totalAvatars + 1, clampedMax);
  const maxAvatars = Math.min(children.length, clampedMax - 1);
  const extraAvatars = Math.max(totalAvatars - clampedMax, totalAvatars - maxAvatars, 0);
  const extraAvatarsElement = renderSurplus ? renderSurplus(extraAvatars) : `+${extraAvatars}`;
  const marginValue = ownerState.spacing && SPACINGS[ownerState.spacing] !== undefined ? SPACINGS[ownerState.spacing] : -ownerState.spacing || -8;
  const externalForwardedProps = {
    slots,
    slotProps: {
      surplus: slotProps.additionalAvatar ?? componentsProps?.additionalAvatar,
      ...componentsProps,
      ...slotProps
    }
  };
  const [SurplusSlot, surplusProps] = (0, _useSlot.default)('surplus', {
    elementType: _Avatar.default,
    externalForwardedProps,
    className: classes.avatar,
    ownerState,
    additionalProps: {
      variant
    }
  });
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(AvatarGroupRoot, {
    as: component,
    ownerState: ownerState,
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ...other,
    style: {
      '--AvatarGroup-spacing': marginValue ? `${marginValue}px` : undefined,
      ...other.style
    },
    children: [extraAvatars ? /*#__PURE__*/(0, _jsxRuntime.jsx)(SurplusSlot, {
      ...surplusProps,
      children: extraAvatarsElement
    }) : null, children.slice(0, maxAvatars).reverse().map(child => {
      return /*#__PURE__*/React.cloneElement(child, {
        className: (0, _clsx.default)(child.props.className, classes.avatar),
        variant: child.props.variant || variant
      });
    })]
  });
});
process.env.NODE_ENV !== "production" ? AvatarGroup.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The avatars to stack.
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
   * The extra props for the slot components.
   * You can override the existing props or add new ones.
   *
   * This prop is an alias for the `slotProps` prop.
   *
   * @deprecated use the `slotProps` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   */
  componentsProps: _propTypes.default.shape({
    additionalAvatar: _propTypes.default.object
  }),
  /**
   * Max avatars to show before +x.
   * @default 5
   */
  max: (0, _chainPropTypes.default)(_propTypes.default.number, props => {
    if (props.max < 2) {
      return new Error(['MUI: The prop `max` should be equal to 2 or above.', 'A value below is clamped to 2.'].join('\n'));
    }
    return null;
  }),
  /**
   * custom renderer of extraAvatars
   * @param {number} surplus number of extra avatars
   * @returns {React.ReactNode} custom element to display
   */
  renderSurplus: _propTypes.default.func,
  /**
   * The props used for each slot inside.
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    additionalAvatar: _propTypes.default.object,
    surplus: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * The components used for each slot inside.
   * @default {}
   */
  slots: _propTypes.default.shape({
    surplus: _propTypes.default.elementType
  }),
  /**
   * Spacing between avatars.
   * @default 'medium'
   */
  spacing: _propTypes.default.oneOfType([_propTypes.default.oneOf(['medium', 'small']), _propTypes.default.number]),
  /**
   * @ignore
   */
  style: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The total number of avatars. Used for calculating the number of extra avatars.
   * @default children.length
   */
  total: _propTypes.default.number,
  /**
   * The variant to use.
   * @default 'circular'
   */
  variant: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['circular', 'rounded', 'square']), _propTypes.default.string])
} : void 0;
var _default = exports.default = AvatarGroup;