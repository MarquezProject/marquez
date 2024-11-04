"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useClearableField = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var _objectWithoutPropertiesLoose2 = _interopRequireDefault(require("@babel/runtime/helpers/objectWithoutPropertiesLoose"));
var React = _interopRequireWildcard(require("react"));
var _useSlotProps2 = _interopRequireDefault(require("@mui/utils/useSlotProps"));
var _IconButton = _interopRequireDefault(require("@mui/material/IconButton"));
var _InputAdornment = _interopRequireDefault(require("@mui/material/InputAdornment"));
var _icons = require("../icons");
var _usePickersTranslations = require("./usePickersTranslations");
var _jsxRuntime = require("react/jsx-runtime");
const _excluded = ["clearable", "onClear", "InputProps", "sx", "slots", "slotProps"],
  _excluded2 = ["ownerState"];
const useClearableField = props => {
  const translations = (0, _usePickersTranslations.usePickersTranslations)();
  const {
      clearable,
      onClear,
      InputProps,
      sx,
      slots,
      slotProps
    } = props,
    other = (0, _objectWithoutPropertiesLoose2.default)(props, _excluded);
  const IconButton = slots?.clearButton ?? _IconButton.default;
  // The spread is here to avoid this bug mui/material-ui#34056
  const _useSlotProps = (0, _useSlotProps2.default)({
      elementType: IconButton,
      externalSlotProps: slotProps?.clearButton,
      ownerState: {},
      className: 'clearButton',
      additionalProps: {
        title: translations.fieldClearLabel
      }
    }),
    iconButtonProps = (0, _objectWithoutPropertiesLoose2.default)(_useSlotProps, _excluded2);
  const EndClearIcon = slots?.clearIcon ?? _icons.ClearIcon;
  const endClearIconProps = (0, _useSlotProps2.default)({
    elementType: EndClearIcon,
    externalSlotProps: slotProps?.clearIcon,
    ownerState: {}
  });
  return (0, _extends2.default)({}, other, {
    InputProps: (0, _extends2.default)({}, InputProps, {
      endAdornment: /*#__PURE__*/(0, _jsxRuntime.jsxs)(React.Fragment, {
        children: [clearable && /*#__PURE__*/(0, _jsxRuntime.jsx)(_InputAdornment.default, {
          position: "end",
          sx: {
            marginRight: InputProps?.endAdornment ? -1 : -1.5
          },
          children: /*#__PURE__*/(0, _jsxRuntime.jsx)(IconButton, (0, _extends2.default)({}, iconButtonProps, {
            onClick: onClear,
            children: /*#__PURE__*/(0, _jsxRuntime.jsx)(EndClearIcon, (0, _extends2.default)({
              fontSize: "small"
            }, endClearIconProps))
          }))
        }), InputProps?.endAdornment]
      })
    }),
    sx: [{
      '& .clearButton': {
        opacity: 1
      },
      '@media (pointer: fine)': {
        '& .clearButton': {
          opacity: 0
        },
        '&:hover, &:focus-within': {
          '.clearButton': {
            opacity: 1
          }
        }
      }
    }, ...(Array.isArray(sx) ? sx : [sx])]
  });
};
exports.useClearableField = useClearableField;