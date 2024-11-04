"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useValidation = useValidation;
var React = _interopRequireWildcard(require("react"));
var _useEventCallback = _interopRequireDefault(require("@mui/utils/useEventCallback"));
var _useUtils = require("../internals/hooks/useUtils");
/**
 * Utility hook to check if a given value is valid based on the provided validation props.
 * @template TDate
 * @template TValue The value type. It will be either the same type as `value` or `null`. It can be in `[start, end]` format in case of range value.
 * @template TError The validation error type. It will be either `string` or a `null`. It can be in `[start, end]` format in case of range value.
 * @param {UseValidationOptions<TValue, TDate, TError, TValidationProps>} options The options to configure the hook.
 * @param {TValue} options.value The value to validate.
 * @param {PickersTimezone} options.timezone The timezone to use for the validation.
 * @param {Validator<TValue, TDate, TError, TValidationProps>} options.validator The validator function to use.
 * @param {TValidationProps} options.props The validation props, they differ depending on the component.
 * @param {(error: TError, value: TValue) => void} options.onError Callback fired when the error associated with the current value changes.
 */
function useValidation(options) {
  const {
    props,
    validator,
    value,
    timezone,
    onError
  } = options;
  const adapter = (0, _useUtils.useLocalizationContext)();
  const previousValidationErrorRef = React.useRef(validator.valueManager.defaultErrorState);
  const validationError = validator({
    adapter,
    value,
    timezone,
    props
  });
  const hasValidationError = validator.valueManager.hasError(validationError);
  React.useEffect(() => {
    if (onError && !validator.valueManager.isSameError(validationError, previousValidationErrorRef.current)) {
      onError(validationError, value);
    }
    previousValidationErrorRef.current = validationError;
  }, [validator, onError, validationError, value]);
  const getValidationErrorForNewValue = (0, _useEventCallback.default)(newValue => {
    return validator({
      adapter,
      value: newValue,
      timezone,
      props
    });
  });
  return {
    validationError,
    hasValidationError,
    getValidationErrorForNewValue
  };
}