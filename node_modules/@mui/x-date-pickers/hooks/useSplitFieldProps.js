'use client';

import _extends from "@babel/runtime/helpers/esm/extends";
import * as React from 'react';
import { DATE_TIME_VALIDATION_PROP_NAMES, DATE_VALIDATION_PROP_NAMES, TIME_VALIDATION_PROP_NAMES } from "../validation/extractValidationProps.js";
const SHARED_FIELD_INTERNAL_PROP_NAMES = ['value', 'defaultValue', 'referenceDate', 'format', 'formatDensity', 'onChange', 'timezone', 'onError', 'shouldRespectLeadingZeros', 'selectedSections', 'onSelectedSectionsChange', 'unstableFieldRef', 'enableAccessibleFieldDOMStructure', 'disabled', 'readOnly', 'dateSeparator'];
/**
 * Split the props received by the field component into:
 * - `internalProps` which are used by the various hooks called by the field component.
 * - `forwardedProps` which are passed to the underlying component.
 * Note that some forwarded props might be used by the hooks as well.
 * For instance, hooks like `useDateField` need props like `autoFocus` to know how to behave.
 * @template TProps, TValueType
 * @param {TProps} props The props received by the field component.
 * @param {TValueType} valueType The type of the field value ('date', 'time', or 'date-time').
 */
export const useSplitFieldProps = (props, valueType) => {
  return React.useMemo(() => {
    const forwardedProps = _extends({}, props);
    const internalProps = {};
    const extractProp = propName => {
      if (forwardedProps.hasOwnProperty(propName)) {
        // @ts-ignore
        internalProps[propName] = forwardedProps[propName];
        delete forwardedProps[propName];
      }
    };
    SHARED_FIELD_INTERNAL_PROP_NAMES.forEach(extractProp);
    if (valueType === 'date') {
      DATE_VALIDATION_PROP_NAMES.forEach(extractProp);
    } else if (valueType === 'time') {
      TIME_VALIDATION_PROP_NAMES.forEach(extractProp);
    } else if (valueType === 'date-time') {
      DATE_VALIDATION_PROP_NAMES.forEach(extractProp);
      TIME_VALIDATION_PROP_NAMES.forEach(extractProp);
      DATE_TIME_VALIDATION_PROP_NAMES.forEach(extractProp);
    }
    return {
      forwardedProps,
      internalProps
    };
  }, [props, valueType]);
};