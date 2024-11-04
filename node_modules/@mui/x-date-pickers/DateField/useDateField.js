'use client';

import { singleItemFieldValueManager, singleItemValueManager } from "../internals/utils/valueManagers.js";
import { useField } from "../internals/hooks/useField/index.js";
import { validateDate } from "../validation/index.js";
import { useSplitFieldProps } from "../hooks/index.js";
import { useDefaultizedDateField } from "../internals/hooks/defaultizedFieldProps.js";
export const useDateField = inProps => {
  const props = useDefaultizedDateField(inProps);
  const {
    forwardedProps,
    internalProps
  } = useSplitFieldProps(props, 'date');
  return useField({
    forwardedProps,
    internalProps,
    valueManager: singleItemValueManager,
    fieldValueManager: singleItemFieldValueManager,
    validator: validateDate,
    valueType: 'date'
  });
};