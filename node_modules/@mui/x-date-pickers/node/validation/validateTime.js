"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.validateTime = void 0;
var _timeUtils = require("../internals/utils/time-utils");
var _valueManagers = require("../internals/utils/valueManagers");
const validateTime = ({
  adapter,
  value,
  timezone,
  props
}) => {
  if (value === null) {
    return null;
  }
  const {
    minTime,
    maxTime,
    minutesStep,
    shouldDisableTime,
    disableIgnoringDatePartForTimeValidation = false,
    disablePast,
    disableFuture
  } = props;
  const now = adapter.utils.date(undefined, timezone);
  const isAfter = (0, _timeUtils.createIsAfterIgnoreDatePart)(disableIgnoringDatePartForTimeValidation, adapter.utils);
  switch (true) {
    case !adapter.utils.isValid(value):
      return 'invalidDate';
    case Boolean(minTime && isAfter(minTime, value)):
      return 'minTime';
    case Boolean(maxTime && isAfter(value, maxTime)):
      return 'maxTime';
    case Boolean(disableFuture && adapter.utils.isAfter(value, now)):
      return 'disableFuture';
    case Boolean(disablePast && adapter.utils.isBefore(value, now)):
      return 'disablePast';
    case Boolean(shouldDisableTime && shouldDisableTime(value, 'hours')):
      return 'shouldDisableTime-hours';
    case Boolean(shouldDisableTime && shouldDisableTime(value, 'minutes')):
      return 'shouldDisableTime-minutes';
    case Boolean(shouldDisableTime && shouldDisableTime(value, 'seconds')):
      return 'shouldDisableTime-seconds';
    case Boolean(minutesStep && adapter.utils.getMinutes(value) % minutesStep !== 0):
      return 'minutesStep';
    default:
      return null;
  }
};
exports.validateTime = validateTime;
validateTime.valueManager = _valueManagers.singleItemValueManager;