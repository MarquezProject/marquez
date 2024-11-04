"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useDateTimePickerDefaultizedProps = useDateTimePickerDefaultizedProps;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var React = _interopRequireWildcard(require("react"));
var _styles = require("@mui/material/styles");
var _useUtils = require("../internals/hooks/useUtils");
var _dateUtils = require("../internals/utils/date-utils");
var _DateTimePickerTabs = require("./DateTimePickerTabs");
var _DateTimePickerToolbar = require("./DateTimePickerToolbar");
var _views = require("../internals/utils/views");
function useDateTimePickerDefaultizedProps(props, name) {
  const utils = (0, _useUtils.useUtils)();
  const defaultDates = (0, _useUtils.useDefaultDates)();
  const themeProps = (0, _styles.useThemeProps)({
    props,
    name
  });
  const ampm = themeProps.ampm ?? utils.is12HourCycleInCurrentLocale();
  const localeText = React.useMemo(() => {
    if (themeProps.localeText?.toolbarTitle == null) {
      return themeProps.localeText;
    }
    return (0, _extends2.default)({}, themeProps.localeText, {
      dateTimePickerToolbarTitle: themeProps.localeText.toolbarTitle
    });
  }, [themeProps.localeText]);
  return (0, _extends2.default)({}, themeProps, (0, _views.applyDefaultViewProps)({
    views: themeProps.views,
    openTo: themeProps.openTo,
    defaultViews: ['year', 'day', 'hours', 'minutes'],
    defaultOpenTo: 'day'
  }), {
    ampm,
    localeText,
    orientation: themeProps.orientation ?? 'portrait',
    // TODO: Remove from public API
    disableIgnoringDatePartForTimeValidation: themeProps.disableIgnoringDatePartForTimeValidation ?? Boolean(themeProps.minDateTime || themeProps.maxDateTime ||
    // allow time clock to correctly check time validity: https://github.com/mui/mui-x/issues/8520
    themeProps.disablePast || themeProps.disableFuture),
    disableFuture: themeProps.disableFuture ?? false,
    disablePast: themeProps.disablePast ?? false,
    minDate: (0, _dateUtils.applyDefaultDate)(utils, themeProps.minDateTime ?? themeProps.minDate, defaultDates.minDate),
    maxDate: (0, _dateUtils.applyDefaultDate)(utils, themeProps.maxDateTime ?? themeProps.maxDate, defaultDates.maxDate),
    minTime: themeProps.minDateTime ?? themeProps.minTime,
    maxTime: themeProps.maxDateTime ?? themeProps.maxTime,
    slots: (0, _extends2.default)({
      toolbar: _DateTimePickerToolbar.DateTimePickerToolbar,
      tabs: _DateTimePickerTabs.DateTimePickerTabs
    }, themeProps.slots),
    slotProps: (0, _extends2.default)({}, themeProps.slotProps, {
      toolbar: (0, _extends2.default)({
        ampm
      }, themeProps.slotProps?.toolbar)
    })
  });
}