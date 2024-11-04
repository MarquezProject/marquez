"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useDatePickerDefaultizedProps = useDatePickerDefaultizedProps;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var React = _interopRequireWildcard(require("react"));
var _styles = require("@mui/material/styles");
var _useUtils = require("../internals/hooks/useUtils");
var _views = require("../internals/utils/views");
var _dateUtils = require("../internals/utils/date-utils");
var _DatePickerToolbar = require("./DatePickerToolbar");
function useDatePickerDefaultizedProps(props, name) {
  const utils = (0, _useUtils.useUtils)();
  const defaultDates = (0, _useUtils.useDefaultDates)();
  const themeProps = (0, _styles.useThemeProps)({
    props,
    name
  });
  const localeText = React.useMemo(() => {
    if (themeProps.localeText?.toolbarTitle == null) {
      return themeProps.localeText;
    }
    return (0, _extends2.default)({}, themeProps.localeText, {
      datePickerToolbarTitle: themeProps.localeText.toolbarTitle
    });
  }, [themeProps.localeText]);
  return (0, _extends2.default)({}, themeProps, {
    localeText
  }, (0, _views.applyDefaultViewProps)({
    views: themeProps.views,
    openTo: themeProps.openTo,
    defaultViews: ['year', 'day'],
    defaultOpenTo: 'day'
  }), {
    disableFuture: themeProps.disableFuture ?? false,
    disablePast: themeProps.disablePast ?? false,
    minDate: (0, _dateUtils.applyDefaultDate)(utils, themeProps.minDate, defaultDates.minDate),
    maxDate: (0, _dateUtils.applyDefaultDate)(utils, themeProps.maxDate, defaultDates.maxDate),
    slots: (0, _extends2.default)({
      toolbar: _DatePickerToolbar.DatePickerToolbar
    }, themeProps.slots)
  });
}