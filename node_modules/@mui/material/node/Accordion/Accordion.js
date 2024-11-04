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
var _chainPropTypes = _interopRequireDefault(require("@mui/utils/chainPropTypes"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _Collapse = _interopRequireDefault(require("../Collapse"));
var _Paper = _interopRequireDefault(require("../Paper"));
var _AccordionContext = _interopRequireDefault(require("./AccordionContext"));
var _useControlled = _interopRequireDefault(require("../utils/useControlled"));
var _useSlot = _interopRequireDefault(require("../utils/useSlot"));
var _accordionClasses = _interopRequireWildcard(require("./accordionClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    square,
    expanded,
    disabled,
    disableGutters
  } = ownerState;
  const slots = {
    root: ['root', !square && 'rounded', expanded && 'expanded', disabled && 'disabled', !disableGutters && 'gutters'],
    heading: ['heading'],
    region: ['region']
  };
  return (0, _composeClasses.default)(slots, _accordionClasses.getAccordionUtilityClass, classes);
};
const AccordionRoot = (0, _zeroStyled.styled)(_Paper.default, {
  name: 'MuiAccordion',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [{
      [`& .${_accordionClasses.default.region}`]: styles.region
    }, styles.root, !ownerState.square && styles.rounded, !ownerState.disableGutters && styles.gutters];
  }
})((0, _memoTheme.default)(({
  theme
}) => {
  const transition = {
    duration: theme.transitions.duration.shortest
  };
  return {
    position: 'relative',
    transition: theme.transitions.create(['margin'], transition),
    overflowAnchor: 'none',
    // Keep the same scrolling position
    '&::before': {
      position: 'absolute',
      left: 0,
      top: -1,
      right: 0,
      height: 1,
      content: '""',
      opacity: 1,
      backgroundColor: (theme.vars || theme).palette.divider,
      transition: theme.transitions.create(['opacity', 'background-color'], transition)
    },
    '&:first-of-type': {
      '&::before': {
        display: 'none'
      }
    },
    [`&.${_accordionClasses.default.expanded}`]: {
      '&::before': {
        opacity: 0
      },
      '&:first-of-type': {
        marginTop: 0
      },
      '&:last-of-type': {
        marginBottom: 0
      },
      '& + &': {
        '&::before': {
          display: 'none'
        }
      }
    },
    [`&.${_accordionClasses.default.disabled}`]: {
      backgroundColor: (theme.vars || theme).palette.action.disabledBackground
    }
  };
}), (0, _memoTheme.default)(({
  theme
}) => ({
  variants: [{
    props: props => !props.square,
    style: {
      borderRadius: 0,
      '&:first-of-type': {
        borderTopLeftRadius: (theme.vars || theme).shape.borderRadius,
        borderTopRightRadius: (theme.vars || theme).shape.borderRadius
      },
      '&:last-of-type': {
        borderBottomLeftRadius: (theme.vars || theme).shape.borderRadius,
        borderBottomRightRadius: (theme.vars || theme).shape.borderRadius,
        // Fix a rendering issue on Edge
        '@supports (-ms-ime-align: auto)': {
          borderBottomLeftRadius: 0,
          borderBottomRightRadius: 0
        }
      }
    }
  }, {
    props: props => !props.disableGutters,
    style: {
      [`&.${_accordionClasses.default.expanded}`]: {
        margin: '16px 0'
      }
    }
  }]
})));
const AccordionHeading = (0, _zeroStyled.styled)('h3', {
  name: 'MuiAccordion',
  slot: 'Heading',
  overridesResolver: (props, styles) => styles.heading
})({
  all: 'unset'
});
const Accordion = /*#__PURE__*/React.forwardRef(function Accordion(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiAccordion'
  });
  const {
    children: childrenProp,
    className,
    defaultExpanded = false,
    disabled = false,
    disableGutters = false,
    expanded: expandedProp,
    onChange,
    square = false,
    slots = {},
    slotProps = {},
    TransitionComponent: TransitionComponentProp,
    TransitionProps: TransitionPropsProp,
    ...other
  } = props;
  const [expanded, setExpandedState] = (0, _useControlled.default)({
    controlled: expandedProp,
    default: defaultExpanded,
    name: 'Accordion',
    state: 'expanded'
  });
  const handleChange = React.useCallback(event => {
    setExpandedState(!expanded);
    if (onChange) {
      onChange(event, !expanded);
    }
  }, [expanded, onChange, setExpandedState]);
  const [summary, ...children] = React.Children.toArray(childrenProp);
  const contextValue = React.useMemo(() => ({
    expanded,
    disabled,
    disableGutters,
    toggle: handleChange
  }), [expanded, disabled, disableGutters, handleChange]);
  const ownerState = {
    ...props,
    square,
    disabled,
    disableGutters,
    expanded
  };
  const classes = useUtilityClasses(ownerState);
  const backwardCompatibleSlots = {
    transition: TransitionComponentProp,
    ...slots
  };
  const backwardCompatibleSlotProps = {
    transition: TransitionPropsProp,
    ...slotProps
  };
  const externalForwardedProps = {
    slots: backwardCompatibleSlots,
    slotProps: backwardCompatibleSlotProps
  };
  const [AccordionHeadingSlot, accordionProps] = (0, _useSlot.default)('heading', {
    elementType: AccordionHeading,
    externalForwardedProps,
    className: classes.heading,
    ownerState
  });
  const [TransitionSlot, transitionProps] = (0, _useSlot.default)('transition', {
    elementType: _Collapse.default,
    externalForwardedProps,
    ownerState
  });
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(AccordionRoot, {
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    square: square,
    ...other,
    children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(AccordionHeadingSlot, {
      ...accordionProps,
      children: /*#__PURE__*/(0, _jsxRuntime.jsx)(_AccordionContext.default.Provider, {
        value: contextValue,
        children: summary
      })
    }), /*#__PURE__*/(0, _jsxRuntime.jsx)(TransitionSlot, {
      in: expanded,
      timeout: "auto",
      ...transitionProps,
      children: /*#__PURE__*/(0, _jsxRuntime.jsx)("div", {
        "aria-labelledby": summary.props.id,
        id: summary.props['aria-controls'],
        role: "region",
        className: classes.region,
        children: children
      })
    })]
  });
});
process.env.NODE_ENV !== "production" ? Accordion.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
   */
  children: (0, _chainPropTypes.default)(_propTypes.default.node.isRequired, props => {
    const summary = React.Children.toArray(props.children)[0];
    if ((0, _reactIs.isFragment)(summary)) {
      return new Error("MUI: The Accordion doesn't accept a Fragment as a child. " + 'Consider providing an array instead.');
    }
    if (! /*#__PURE__*/React.isValidElement(summary)) {
      return new Error('MUI: Expected the first child of Accordion to be a valid element.');
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
   * If `true`, expands the accordion by default.
   * @default false
   */
  defaultExpanded: _propTypes.default.bool,
  /**
   * If `true`, the component is disabled.
   * @default false
   */
  disabled: _propTypes.default.bool,
  /**
   * If `true`, it removes the margin between two expanded accordion items and the increase of height.
   * @default false
   */
  disableGutters: _propTypes.default.bool,
  /**
   * If `true`, expands the accordion, otherwise collapse it.
   * Setting this prop enables control over the accordion.
   */
  expanded: _propTypes.default.bool,
  /**
   * Callback fired when the expand/collapse state is changed.
   *
   * @param {React.SyntheticEvent} event The event source of the callback. **Warning**: This is a generic event not a change event.
   * @param {boolean} expanded The `expanded` state of the accordion.
   */
  onChange: _propTypes.default.func,
  /**
   * The props used for each slot inside.
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    heading: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    transition: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * The components used for each slot inside.
   * @default {}
   */
  slots: _propTypes.default.shape({
    heading: _propTypes.default.elementType,
    transition: _propTypes.default.elementType
  }),
  /**
   * If `true`, rounded corners are disabled.
   * @default false
   */
  square: _propTypes.default.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The component used for the transition.
   * [Follow this guide](https://mui.com/material-ui/transitions/#transitioncomponent-prop) to learn more about the requirements for this component.
   */
  TransitionComponent: _propTypes.default.elementType,
  /**
   * Props applied to the transition element.
   * By default, the element is based on this [`Transition`](https://reactcommunity.org/react-transition-group/transition/) component.
   */
  TransitionProps: _propTypes.default.object
} : void 0;
var _default = exports.default = Accordion;