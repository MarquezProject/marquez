'use client';

import _extends from "@babel/runtime/helpers/esm/extends";
import * as React from 'react';
import useEventCallback from '@mui/utils/useEventCallback';
import { useIsDateDisabled } from "./useIsDateDisabled.js";
import { useUtils } from "../internals/hooks/useUtils.js";
import { singleItemValueManager } from "../internals/utils/valueManagers.js";
import { SECTION_TYPE_GRANULARITY } from "../internals/utils/getDefaultReferenceDate.js";
export const createCalendarStateReducer = (reduceAnimations, disableSwitchToMonthOnDayFocus, utils) => (state, action) => {
  switch (action.type) {
    case 'changeMonth':
      return _extends({}, state, {
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
        return _extends({}, state, {
          currentMonth: newCurrentMonth
        });
      }
    case 'finishMonthSwitchingAnimation':
      return _extends({}, state, {
        isMonthSwitchingAnimating: false
      });
    case 'changeFocusedDay':
      {
        if (state.focusedDay != null && action.focusedDay != null && utils.isSameDay(action.focusedDay, state.focusedDay)) {
          return state;
        }
        const needMonthSwitch = action.focusedDay != null && !disableSwitchToMonthOnDayFocus && !utils.isSameMonth(state.currentMonth, action.focusedDay);
        return _extends({}, state, {
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
export const useCalendarState = params => {
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
  const utils = useUtils();
  const reducerFn = React.useRef(createCalendarStateReducer(Boolean(reduceAnimations), disableSwitchToMonthOnDayFocus, utils)).current;
  const referenceDate = React.useMemo(() => {
    return singleItemValueManager.getInitialReferenceValue({
      value,
      utils,
      timezone,
      props: params,
      referenceDate: referenceDateProp,
      granularity: SECTION_TYPE_GRANULARITY.day
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
    dispatch(_extends({
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
  const isDateDisabled = useIsDateDisabled({
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
  const changeFocusedDay = useEventCallback((newFocusedDate, withoutMonthSwitchingAnimation) => {
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