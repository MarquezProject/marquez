"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useMeridiemMode = useMeridiemMode;
exports.useNextMonthDisabled = useNextMonthDisabled;
exports.usePreviousMonthDisabled = usePreviousMonthDisabled;
var React = _interopRequireWildcard(require("react"));
var _useUtils = require("./useUtils");
var _timeUtils = require("../utils/time-utils");
function useNextMonthDisabled(month, {
  disableFuture,
  maxDate,
  timezone
}) {
  const utils = (0, _useUtils.useUtils)();
  return React.useMemo(() => {
    const now = utils.date(undefined, timezone);
    const lastEnabledMonth = utils.startOfMonth(disableFuture && utils.isBefore(now, maxDate) ? now : maxDate);
    return !utils.isAfter(lastEnabledMonth, month);
  }, [disableFuture, maxDate, month, utils, timezone]);
}
function usePreviousMonthDisabled(month, {
  disablePast,
  minDate,
  timezone
}) {
  const utils = (0, _useUtils.useUtils)();
  return React.useMemo(() => {
    const now = utils.date(undefined, timezone);
    const firstEnabledMonth = utils.startOfMonth(disablePast && utils.isAfter(now, minDate) ? now : minDate);
    return !utils.isBefore(firstEnabledMonth, month);
  }, [disablePast, minDate, month, utils, timezone]);
}
function useMeridiemMode(date, ampm, onChange, selectionState) {
  const utils = (0, _useUtils.useUtils)();
  const meridiemMode = (0, _timeUtils.getMeridiem)(date, utils);
  const handleMeridiemChange = React.useCallback(mode => {
    const timeWithMeridiem = date == null ? null : (0, _timeUtils.convertToMeridiem)(date, mode, Boolean(ampm), utils);
    onChange(timeWithMeridiem, selectionState ?? 'partial');
  }, [ampm, date, onChange, selectionState, utils]);
  return {
    meridiemMode,
    handleMeridiemChange
  };
}