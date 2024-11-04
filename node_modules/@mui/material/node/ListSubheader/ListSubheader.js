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
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _listSubheaderClasses = require("./listSubheaderClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    color,
    disableGutters,
    inset,
    disableSticky
  } = ownerState;
  const slots = {
    root: ['root', color !== 'default' && `color${(0, _capitalize.default)(color)}`, !disableGutters && 'gutters', inset && 'inset', !disableSticky && 'sticky']
  };
  return (0, _composeClasses.default)(slots, _listSubheaderClasses.getListSubheaderUtilityClass, classes);
};
const ListSubheaderRoot = (0, _zeroStyled.styled)('li', {
  name: 'MuiListSubheader',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.color !== 'default' && styles[`color${(0, _capitalize.default)(ownerState.color)}`], !ownerState.disableGutters && styles.gutters, ownerState.inset && styles.inset, !ownerState.disableSticky && styles.sticky];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  boxSizing: 'border-box',
  lineHeight: '48px',
  listStyle: 'none',
  color: (theme.vars || theme).palette.text.secondary,
  fontFamily: theme.typography.fontFamily,
  fontWeight: theme.typography.fontWeightMedium,
  fontSize: theme.typography.pxToRem(14),
  variants: [{
    props: {
      color: 'primary'
    },
    style: {
      color: (theme.vars || theme).palette.primary.main
    }
  }, {
    props: {
      color: 'inherit'
    },
    style: {
      color: 'inherit'
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
    }) => ownerState.inset,
    style: {
      paddingLeft: 72
    }
  }, {
    props: ({
      ownerState
    }) => !ownerState.disableSticky,
    style: {
      position: 'sticky',
      top: 0,
      zIndex: 1,
      backgroundColor: (theme.vars || theme).palette.background.paper
    }
  }]
})));
const ListSubheader = /*#__PURE__*/React.forwardRef(function ListSubheader(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiListSubheader'
  });
  const {
    className,
    color = 'default',
    component = 'li',
    disableGutters = false,
    disableSticky = false,
    inset = false,
    ...other
  } = props;
  const ownerState = {
    ...props,
    color,
    component,
    disableGutters,
    disableSticky,
    inset
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(ListSubheaderRoot, {
    as: component,
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other
  });
});
if (ListSubheader) {
  ListSubheader.muiSkipListHighlight = true;
}
process.env.NODE_ENV !== "production" ? ListSubheader.propTypes /* remove-proptypes */ = {
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
   * The color of the component. It supports those theme colors that make sense for this component.
   * @default 'default'
   */
  color: _propTypes.default.oneOf(['default', 'inherit', 'primary']),
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * If `true`, the List Subheader will not have gutters.
   * @default false
   */
  disableGutters: _propTypes.default.bool,
  /**
   * If `true`, the List Subheader will not stick to the top during scroll.
   * @default false
   */
  disableSticky: _propTypes.default.bool,
  /**
   * If `true`, the List Subheader is indented.
   * @default false
   */
  inset: _propTypes.default.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = ListSubheader;