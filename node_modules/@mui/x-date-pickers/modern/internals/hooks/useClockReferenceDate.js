import * as React from 'react';
import { singleItemValueManager } from "../utils/valueManagers.js";
import { getTodayDate } from "../utils/date-utils.js";
import { SECTION_TYPE_GRANULARITY } from "../utils/getDefaultReferenceDate.js";
export const useClockReferenceDate = ({
  value,
  referenceDate: referenceDateProp,
  utils,
  props,
  timezone
}) => {
  const referenceDate = React.useMemo(() => singleItemValueManager.getInitialReferenceValue({
    value,
    utils,
    props,
    referenceDate: referenceDateProp,
    granularity: SECTION_TYPE_GRANULARITY.day,
    timezone,
    getTodayDate: () => getTodayDate(utils, timezone, 'date')
  }),
  // We only want to compute the reference date on mount.
  [] // eslint-disable-line react-hooks/exhaustive-deps
  );
  return value ?? referenceDate;
};