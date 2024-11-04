'use client';

import { singleItemFieldValueManager, singleItemValueManager } from "../internals/utils/valueManagers.js";
import { useField } from "../internals/hooks/useField/index.js";
import { validateTime } from "../validation/index.js";
import { useSplitFieldProps } from "../hooks/index.js";
import { useDefaultizedTimeField } from "../internals/hooks/defaultizedFieldProps.js";
export const useTimeField = inProps => {
  const props = useDefaultizedTimeField(inProps);
  const {
    forwardedProps,
    internalProps
  } = useSplitFieldProps(props, 'time');
  return useField({
    forwardedProps,
    internalProps,
    valueManager: singleItemValueManager,
    fieldValueManager: singleItemFieldValueManager,
    validator: validateTime,
    valueType: 'time'
  });
};