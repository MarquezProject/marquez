'use client';

import { singleItemFieldValueManager, singleItemValueManager } from "../internals/utils/valueManagers.js";
import { useField } from "../internals/hooks/useField/index.js";
import { validateDateTime } from "../validation/index.js";
import { useSplitFieldProps } from "../hooks/index.js";
import { useDefaultizedDateTimeField } from "../internals/hooks/defaultizedFieldProps.js";
export const useDateTimeField = inProps => {
  const props = useDefaultizedDateTimeField(inProps);
  const {
    forwardedProps,
    internalProps
  } = useSplitFieldProps(props, 'date-time');
  return useField({
    forwardedProps,
    internalProps,
    valueManager: singleItemValueManager,
    fieldValueManager: singleItemFieldValueManager,
    validator: validateDateTime,
    valueType: 'date-time'
  });
};