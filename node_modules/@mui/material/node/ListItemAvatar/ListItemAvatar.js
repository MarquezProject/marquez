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
var _ListContext = _interopRequireDefault(require("../List/ListContext"));
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _listItemAvatarClasses = require("./listItemAvatarClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    alignItems,
    classes
  } = ownerState;
  const slots = {
    root: ['root', alignItems === 'flex-start' && 'alignItemsFlexStart']
  };
  return (0, _composeClasses.default)(slots, _listItemAvatarClasses.getListItemAvatarUtilityClass, classes);
};
const ListItemAvatarRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiListItemAvatar',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.alignItems === 'flex-start' && styles.alignItemsFlexStart];
  }
})({
  minWidth: 56,
  flexShrink: 0,
  variants: [{
    props: {
      alignItems: 'flex-start'
    },
    style: {
      marginTop: 8
    }
  }]
});

/**
 * A simple wrapper to apply `List` styles to an `Avatar`.
 */
const ListItemAvatar = /*#__PURE__*/React.forwardRef(function ListItemAvatar(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiListItemAvatar'
  });
  const {
    className,
    ...other
  } = props;
  const context = React.useContext(_ListContext.default);
  const ownerState = {
    ...props,
    alignItems: context.alignItems
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(ListItemAvatarRoot, {
    className: (0, _clsx.default)(classes.root, className),
    ownerState: ownerState,
    ref: ref,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? ListItemAvatar.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component, normally an `Avatar`.
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
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = ListItemAvatar;