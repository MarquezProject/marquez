"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.PickersActionBar = PickersActionBar;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var _objectWithoutPropertiesLoose2 = _interopRequireDefault(require("@babel/runtime/helpers/objectWithoutPropertiesLoose"));
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _Button = _interopRequireDefault(require("@mui/material/Button"));
var _DialogActions = _interopRequireDefault(require("@mui/material/DialogActions"));
var _usePickersTranslations = require("../hooks/usePickersTranslations");
var _jsxRuntime = require("react/jsx-runtime");
const _excluded = ["onAccept", "onClear", "onCancel", "onSetToday", "actions"];
/**
 * Demos:
 *
 * - [Custom slots and subcomponents](https://mui.com/x/react-date-pickers/custom-components/)
 * - [Custom layout](https://mui.com/x/react-date-pickers/custom-layout/)
 *
 * API:
 *
 * - [PickersActionBar API](https://mui.com/x/api/date-pickers/pickers-action-bar/)
 */
function PickersActionBar(props) {
  const {
      onAccept,
      onClear,
      onCancel,
      onSetToday,
      actions
    } = props,
    other = (0, _objectWithoutPropertiesLoose2.default)(props, _excluded);
  const translations = (0, _usePickersTranslations.usePickersTranslations)();
  if (actions == null || actions.length === 0) {
    return null;
  }
  const buttons = actions?.map(actionType => {
    switch (actionType) {
      case 'clear':
        return /*#__PURE__*/(0, _jsxRuntime.jsx)(_Button.default, {
          onClick: onClear,
          children: translations.clearButtonLabel
        }, actionType);
      case 'cancel':
        return /*#__PURE__*/(0, _jsxRuntime.jsx)(_Button.default, {
          onClick: onCancel,
          children: translations.cancelButtonLabel
        }, actionType);
      case 'accept':
        return /*#__PURE__*/(0, _jsxRuntime.jsx)(_Button.default, {
          onClick: onAccept,
          children: translations.okButtonLabel
        }, actionType);
      case 'today':
        return /*#__PURE__*/(0, _jsxRuntime.jsx)(_Button.default, {
          onClick: onSetToday,
          children: translations.todayButtonLabel
        }, actionType);
      default:
        return null;
    }
  });
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_DialogActions.default, (0, _extends2.default)({}, other, {
    children: buttons
  }));
}
process.env.NODE_ENV !== "production" ? PickersActionBar.propTypes = {
  // ----------------------------- Warning --------------------------------
  // | These PropTypes are generated from the TypeScript type definitions |
  // | To update them edit the TypeScript types and run "pnpm proptypes"  |
  // ----------------------------------------------------------------------
  /**
   * Ordered array of actions to display.
   * If empty, does not display that action bar.
   * @default `['cancel', 'accept']` for mobile and `[]` for desktop
   */
  actions: _propTypes.default.arrayOf(_propTypes.default.oneOf(['accept', 'cancel', 'clear', 'today']).isRequired),
  /**
   * If `true`, the actions do not have additional margin.
   * @default false
   */
  disableSpacing: _propTypes.default.bool,
  onAccept: _propTypes.default.func.isRequired,
  onCancel: _propTypes.default.func.isRequired,
  onClear: _propTypes.default.func.isRequired,
  onSetToday: _propTypes.default.func.isRequired,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;