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
var _colorManipulator = require("@mui/system/colorManipulator");
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _MoreHoriz = _interopRequireDefault(require("../internal/svg-icons/MoreHoriz"));
var _ButtonBase = _interopRequireDefault(require("../ButtonBase"));
var _jsxRuntime = require("react/jsx-runtime");
const BreadcrumbCollapsedButton = (0, _zeroStyled.styled)(_ButtonBase.default)((0, _memoTheme.default)(({
  theme
}) => ({
  display: 'flex',
  marginLeft: `calc(${theme.spacing(1)} * 0.5)`,
  marginRight: `calc(${theme.spacing(1)} * 0.5)`,
  ...(theme.palette.mode === 'light' ? {
    backgroundColor: theme.palette.grey[100],
    color: theme.palette.grey[700]
  } : {
    backgroundColor: theme.palette.grey[700],
    color: theme.palette.grey[100]
  }),
  borderRadius: 2,
  '&:hover, &:focus': {
    ...(theme.palette.mode === 'light' ? {
      backgroundColor: theme.palette.grey[200]
    } : {
      backgroundColor: theme.palette.grey[600]
    })
  },
  '&:active': {
    boxShadow: theme.shadows[0],
    ...(theme.palette.mode === 'light' ? {
      backgroundColor: (0, _colorManipulator.emphasize)(theme.palette.grey[200], 0.12)
    } : {
      backgroundColor: (0, _colorManipulator.emphasize)(theme.palette.grey[600], 0.12)
    })
  }
})));
const BreadcrumbCollapsedIcon = (0, _zeroStyled.styled)(_MoreHoriz.default)({
  width: 24,
  height: 16
});

/**
 * @ignore - internal component.
 */
function BreadcrumbCollapsed(props) {
  const {
    slots = {},
    slotProps = {},
    ...otherProps
  } = props;
  const ownerState = props;
  return /*#__PURE__*/(0, _jsxRuntime.jsx)("li", {
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(BreadcrumbCollapsedButton, {
      focusRipple: true,
      ...otherProps,
      ownerState: ownerState,
      children: /*#__PURE__*/(0, _jsxRuntime.jsx)(BreadcrumbCollapsedIcon, {
        as: slots.CollapsedIcon,
        ownerState: ownerState,
        ...slotProps.collapsedIcon
      })
    })
  });
}
process.env.NODE_ENV !== "production" ? BreadcrumbCollapsed.propTypes = {
  /**
   * The props used for the CollapsedIcon slot.
   * @default {}
   */
  slotProps: _propTypes.default.shape({
    collapsedIcon: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * The components used for each slot inside the BreadcumbCollapsed.
   * Either a string to use a HTML element or a component.
   * @default {}
   */
  slots: _propTypes.default.shape({
    CollapsedIcon: _propTypes.default.elementType
  }),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.object
} : void 0;
var _default = exports.default = BreadcrumbCollapsed;