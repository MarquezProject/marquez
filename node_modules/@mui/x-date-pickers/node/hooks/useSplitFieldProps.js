"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useSplitFieldProps = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var React = _interopRequireWildcard(require("react"));
var _extractValidationProps = require("../validation/extractValidationProps");
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
const useSplitFieldProps = (props, valueType) => {
  return React.useMemo(() => {
    const forwardedProps = (0, _extends2.default)({}, props);
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
      _extractValidationProps.DATE_VALIDATION_PROP_NAMES.forEach(extractProp);
    } else if (valueType === 'time') {
      _extractValidationProps.TIME_VALIDATION_PROP_NAMES.forEach(extractProp);
    } else if (valueType === 'date-time') {
      _extractValidationProps.DATE_VALIDATION_PROP_NAMES.forEach(extractProp);
      _extractValidationProps.TIME_VALIDATION_PROP_NAMES.forEach(extractProp);
      _extractValidationProps.DATE_TIME_VALIDATION_PROP_NAMES.forEach(extractProp);
    }
    return {
      forwardedProps,
      internalProps
    };
  }, [props, valueType]);
};
exports.useSplitFieldProps = useSplitFieldProps;