"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useCalendarState = exports.createCalendarStateReducer = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var React = _interopRequireWildcard(require("react"));
var _useEventCallback = _interopRequireDefault(require("@mui/utils/useEventCallback"));
var _useIsDateDisabled = require("./useIsDateDisabled");
var _useUtils = require("../internals/hooks/useUtils");
var _valueManagers = require("../internals/utils/valueManagers");
var _getDefaultReferenceDate = require("../internals/utils/getDefaultReferenceDate");
const createCalendarStateReducer = (reduceAnimations, disableSwitchToMonthOnDayFocus, utils) => (state, action) => {
  switch (action.type) {
    case 'changeMonth':
      return (0, _extends2.default)({}, state, {
        slideDirection: action.direction,
        currentMonth: action.newMonth,
        isMonthSwitchingAnimating: !reduceAnimations
      });
    case 'changeMonthTimezone':
      {
        const newTimezone = action.newTimezone;
        if (utils.getTimezone(state.currentMonth) === newTimezone) {
          return state;
        }
        let newCurrentMonth = utils.setTimezone(state.currentMonth, newTimezone);
        if (utils.getMonth(newCurrentMonth) !== utils.getMonth(state.currentMonth)) {
          newCurrentMonth = utils.setMonth(newCurrentMonth, utils.getMonth(state.currentMonth));
        }
        return (0, _extends2.default)({}, state, {
          currentMonth: newCurrentMonth
        });
      }
    case 'finishMonthSwitchingAnimation':
      return (0, _extends2.default)({}, state, {
        isMonthSwitchingAnimating: false
      });
    case 'changeFocusedDay':
      {
        if (state.focusedDay != null && action.focusedDay != null && utils.isSameDay(action.focusedDay, state.focusedDay)) {
          return state;
        }
        const needMonthSwitch = action.focusedDay != null && !disableSwitchToMonthOnDayFocus && !utils.isSameMonth(state.currentMonth, action.focusedDay);
        return (0, _extends2.default)({}, state, {
          focusedDay: action.focusedDay,
          isMonthSwitchingAnimating: needMonthSwitch && !reduceAnimations && !action.withoutMonthSwitchingAnimation,
          currentMonth: needMonthSwitch ? utils.startOfMonth(action.focusedDay) : state.currentMonth,
          slideDirection: action.focusedDay != null && utils.isAfterDay(action.focusedDay, state.currentMonth) ? 'left' : 'right'
        });
      }
    default:
      throw new Error('missing support');
  }
};
exports.createCalendarStateReducer = createCalendarStateReducer;
const useCalendarState = params => {
  const {
    value,
    referenceDate: referenceDateProp,
    disableFuture,
    disablePast,
    disableSwitchToMonthOnDayFocus = false,
    maxDate,
    minDate,
    onMonthChange,
    reduceAnimations,
    shouldDisableDate,
    timezone
  } = params;
  const utils = (0, _useUtils.useUtils)();
  const reducerFn = React.useRef(createCalendarStateReducer(Boolean(reduceAnimations), disableSwitchToMonthOnDayFocus, utils)).current;
  const referenceDate = React.useMemo(() => {
    return _valueManagers.singleItemValueManager.getInitialReferenceValue({
      value,
      utils,
      timezone,
      props: params,
      referenceDate: referenceDateProp,
      granularity: _getDefaultReferenceDate.SECTION_TYPE_GRANULARITY.day
    });
  },
  // We want the `referenceDate` to update on prop and `timezone` change (https://github.com/mui/mui-x/issues/10804)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  [referenceDateProp, timezone]);
  const [calendarState, dispatch] = React.useReducer(reducerFn, {
    isMonthSwitchingAnimating: false,
    focusedDay: referenceDate,
    currentMonth: utils.startOfMonth(referenceDate),
    slideDirection: 'left'
  });

  // Ensure that `calendarState.currentMonth` timezone is updated when `referenceDate` (or timezone changes)
  // https://github.com/mui/mui-x/issues/10804
  React.useEffect(() => {
    dispatch({
      type: 'changeMonthTimezone',
      newTimezone: utils.getTimezone(referenceDate)
    });
  }, [referenceDate, utils]);
  const handleChangeMonth = React.useCallback(payload => {
    dispatch((0, _extends2.default)({
      type: 'changeMonth'
    }, payload));
    if (onMonthChange) {
      onMonthChange(payload.newMonth);
    }
  }, [onMonthChange]);
  const changeMonth = React.useCallback(newDate => {
    const newDateRequested = newDate;
    if (utils.isSameMonth(newDateRequested, calendarState.currentMonth)) {
      return;
    }
    handleChangeMonth({
      newMonth: utils.startOfMonth(newDateRequested),
      direction: utils.isAfterDay(newDateRequested, calendarState.currentMonth) ? 'left' : 'right'
    });
  }, [calendarState.currentMonth, handleChangeMonth, utils]);
  const isDateDisabled = (0, _useIsDateDisabled.useIsDateDisabled)({
    shouldDisableDate,
    minDate,
    maxDate,
    disableFuture,
    disablePast,
    timezone
  });
  const onMonthSwitchingAnimationEnd = React.useCallback(() => {
    dispatch({
      type: 'finishMonthSwitchingAnimation'
    });
  }, []);
  const changeFocusedDay = (0, _useEventCallback.default)((newFocusedDate, withoutMonthSwitchingAnimation) => {
    if (!isDateDisabled(newFocusedDate)) {
      dispatch({
        type: 'changeFocusedDay',
        focusedDay: newFocusedDate,
        withoutMonthSwitchingAnimation
      });
    }
  });
  return {
    referenceDate,
    calendarState,
    changeMonth,
    changeFocusedDay,
    isDateDisabled,
    onMonthSwitchingAnimationEnd,
    handleChangeMonth
  };
};
exports.useCalendarState = useCalendarState;