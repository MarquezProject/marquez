"use strict";
'use client';

/* eslint-disable jsx-a11y/aria-role */
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
var _RtlProvider = require("@mui/system/RtlProvider");
var _useSlotProps = _interopRequireDefault(require("@mui/utils/useSlotProps"));
var _KeyboardArrowLeft = _interopRequireDefault(require("../internal/svg-icons/KeyboardArrowLeft"));
var _KeyboardArrowRight = _interopRequireDefault(require("../internal/svg-icons/KeyboardArrowRight"));
var _ButtonBase = _interopRequireDefault(require("../ButtonBase"));
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _tabScrollButtonClasses = _interopRequireWildcard(require("./tabScrollButtonClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    orientation,
    disabled
  } = ownerState;
  const slots = {
    root: ['root', orientation, disabled && 'disabled']
  };
  return (0, _composeClasses.default)(slots, _tabScrollButtonClasses.getTabScrollButtonUtilityClass, classes);
};
const TabScrollButtonRoot = (0, _zeroStyled.styled)(_ButtonBase.default, {
  name: 'MuiTabScrollButton',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.orientation && styles[ownerState.orientation]];
  }
})({
  width: 40,
  flexShrink: 0,
  opacity: 0.8,
  [`&.${_tabScrollButtonClasses.default.disabled}`]: {
    opacity: 0
  },
  variants: [{
    props: {
      orientation: 'vertical'
    },
    style: {
      width: '100%',
      height: 40,
      '& svg': {
        transform: 'var(--TabScrollButton-svgRotate)'
      }
    }
  }]
});
const TabScrollButton = /*#__PURE__*/React.forwardRef(function TabScrollButton(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiTabScrollButton'
  });
  const {
    className,
    slots = {},
    slotProps = {},
    direction,
    orientation,
    disabled,
    ...other
  } = props;
  const isRtl = (0, _RtlProvider.useRtl)();
  const ownerState = {
    isRtl,
    ...props
  };
  const classes = useUtilityClasses(ownerState);
  const StartButtonIcon = slots.StartScrollButtonIcon ?? _KeyboardArrowLeft.default;
  const EndButtonIcon = slots.EndScrollButtonIcon ?? _KeyboardArrowRight.default;
  const startButtonIconProps = (0, _useSlotProps.default)({
    elementType: StartButtonIcon,
    externalSlotProps: slotProps.startScrollButtonIcon,
    additionalProps: {
      fontSize: 'small'
    },
    ownerState
  });
  const endButtonIconProps = (0, _useSlotProps.default)({
    elementType: EndButtonIcon,
    externalSlotProps: slotProps.endScrollButtonIcon,
    additionalProps: {
      fontSize: 'small'
    },
    ownerState
  });
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(TabScrollButtonRoot, {
    component: "div",
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    role: null,
    ownerState: ownerState,
    tabIndex: null,
    ...other,
    style: {
      ...other.style,
      ...(orientation === 'vertical' && {
        '--TabScrollButton-svgRotate': `rotate(${isRtl ? -90 : 90}deg)`
      })
    },
    children: direction === 'left' ? /*#__PURE__*/(0, _jsxRuntime.jsx)(StartButtonIcon, {
      ...startButtonIconProps
    }) : /*#__PURE__*/(0, _jsxRuntime.jsx)(EndButtonIcon, {
      ...endButtonIconProps
    })
  });
});
process.env.NODE_ENV !== "production" ? TabScrollButton.propTypes /* remove-proptypes */ = {
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
   * The direction the button should indicate.
   */
  direction: _propTypes.default.oneOf(['left', 'right']).isRequired,
  /**
   * If `true`, the component is disabled.
   * @default false
   */
  disabled: _propTypes.default.bool,
  /**
   * The component orientation (layout flow direction).
   */
  orientation: _propTypes.default.oneOf(['horizontal', 'vertical']).isRequired,
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
   * @ignore
   */
  style: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = TabScrollButton;