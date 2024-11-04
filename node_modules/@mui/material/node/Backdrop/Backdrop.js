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
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _useSlot = _interopRequireDefault(require("../utils/useSlot"));
var _Fade = _interopRequireDefault(require("../Fade"));
var _backdropClasses = require("./backdropClasses");
var _jsxRuntime = require("react/jsx-runtime");
const removeOwnerState = props => {
  const {
    ownerState,
    ...rest
  } = props;
  return rest;
};
const useUtilityClasses = ownerState => {
  const {
    classes,
    invisible
  } = ownerState;
  const slots = {
    root: ['root', invisible && 'invisible']
  };
  return (0, _composeClasses.default)(slots, _backdropClasses.getBackdropUtilityClass, classes);
};
const BackdropRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiBackdrop',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.invisible && styles.invisible];
  }
})({
  position: 'fixed',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  right: 0,
  bottom: 0,
  top: 0,
  left: 0,
  backgroundColor: 'rgba(0, 0, 0, 0.5)',
  WebkitTapHighlightColor: 'transparent',
  variants: [{
    props: {
      invisible: true
    },
    style: {
      backgroundColor: 'transparent'
    }
  }]
});
const Backdrop = /*#__PURE__*/React.forwardRef(function Backdrop(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiBackdrop'
  });
  const {
    children,
    className,
    component = 'div',
    invisible = false,
    open,
    components = {},
    componentsProps = {},
    slotProps = {},
    slots = {},
    TransitionComponent: TransitionComponentProp,
    transitionDuration,
    ...other
  } = props;
  const ownerState = {
    ...props,
    component,
    invisible
  };
  const classes = useUtilityClasses(ownerState);
  const backwardCompatibleSlots = {
    transition: TransitionComponentProp,
    root: components.Root,
    ...slots
  };
  const backwardCompatibleSlotProps = {
    ...componentsProps,
    ...slotProps
  };
  const externalForwardedProps = {
    slots: backwardCompatibleSlots,
    slotProps: backwardCompatibleSlotProps
  };
  const [RootSlot, rootProps] = (0, _useSlot.default)('root', {
    elementType: BackdropRoot,
    externalForwardedProps,
    className: (0, _clsx.default)(classes.root, className),
    ownerState
  });
  const [TransitionSlot, transitionProps] = (0, _useSlot.default)('transition', {
    elementType: _Fade.default,
    externalForwardedProps,
    ownerState
  });
  const transitionPropsRemoved = removeOwnerState(transitionProps);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(TransitionSlot, {
    in: open,
    timeout: transitionDuration,
    ...other,
    ...transitionPropsRemoved,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(RootSlot, {
      "aria-hidden": true,
      ...rootProps,
      classes: classes,
      ref: ref,
      children: children
    })
  });
});
process.env.NODE_ENV !== "production" ? Backdrop.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
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
   * The components used for each slot inside.
   *
   * @deprecated Use the `slots` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   *
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
   *
   * @default {}
   */
  componentsProps: _propTypes.default.shape({
    root: _propTypes.default.object
  }),
  /**
   * If `true`, the backdrop is invisible.
   * It can be used when rendering a popover or a custom select component.
   * @default false
   */
  invisible: _propTypes.default.bool,
  /**
   * If `true`, the component is shown.
   */
  open: _propTypes.default.bool.isRequired,
  /**
   * The props used for each slot inside.
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    root: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    transition: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * The components used for each slot inside.
   * @default {}
   */
  slots: _propTypes.default.shape({
    root: _propTypes.default.elementType,
    transition: _propTypes.default.elementType
  }),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The component used for the transition.
   * [Follow this guide](https://mui.com/material-ui/transitions/#transitioncomponent-prop) to learn more about the requirements for this component.
   * @default Fade
   */
  TransitionComponent: _propTypes.default.elementType,
  /**
   * The duration for the transition, in milliseconds.
   * You may specify a single timeout for all transitions, or individually with an object.
   */
  transitionDuration: _propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.shape({
    appear: _propTypes.default.number,
    enter: _propTypes.default.number,
    exit: _propTypes.default.number
  })])
} : void 0;
var _default = exports.default = Backdrop;