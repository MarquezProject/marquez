"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = createBox;
var React = _interopRequireWildcard(require("react"));
var _clsx = _interopRequireDefault(require("clsx"));
var _styledEngine = _interopRequireDefault(require("@mui/styled-engine"));
var _styleFunctionSx = _interopRequireWildcard(require("../styleFunctionSx"));
var _useTheme = _interopRequireDefault(require("../useTheme"));
var _jsxRuntime = require("react/jsx-runtime");
function createBox(options = {}) {
  const {
    themeId,
    defaultTheme,
    defaultClassName = 'MuiBox-root',
    generateClassName
  } = options;
  const BoxRoot = (0, _styledEngine.default)('div', {
    shouldForwardProp: prop => prop !== 'theme' && prop !== 'sx' && prop !== 'as'
  })(_styleFunctionSx.default);
  const Box = /*#__PURE__*/React.forwardRef(function Box(inProps, ref) {
    const theme = (0, _useTheme.default)(defaultTheme);
    const {
      className,
      component = 'div',
      ...other
    } = (0, _styleFunctionSx.extendSxProp)(inProps);
    return /*#__PURE__*/(0, _jsxRuntime.jsx)(BoxRoot, {
      as: component,
      ref: ref,
      className: (0, _clsx.default)(className, generateClassName ? generateClassName(defaultClassName) : defaultClassName),
      theme: themeId ? theme[themeId] || theme : theme,
      ...other
    });
  });
  return Box;
}