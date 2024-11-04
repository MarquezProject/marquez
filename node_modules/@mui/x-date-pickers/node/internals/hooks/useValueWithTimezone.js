"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useValueWithTimezone = exports.useControlledValueWithTimezone = void 0;
var React = _interopRequireWildcard(require("react"));
var _useEventCallback = _interopRequireDefault(require("@mui/utils/useEventCallback"));
var _useControlled = _interopRequireDefault(require("@mui/utils/useControlled"));
var _useUtils = require("./useUtils");
/**
 * Hooks making sure that:
 * - The value returned by `onChange` always have the timezone of `props.value` or `props.defaultValue` if defined
 * - The value rendered is always the one from `props.timezone` if defined
 */
const useValueWithTimezone = ({
  timezone: timezoneProp,
  value: valueProp,
  defaultValue,
  onChange,
  valueManager
}) => {
  const utils = (0, _useUtils.useUtils)();
  const firstDefaultValue = React.useRef(defaultValue);
  const inputValue = valueProp ?? firstDefaultValue.current ?? valueManager.emptyValue;
  const inputTimezone = React.useMemo(() => valueManager.getTimezone(utils, inputValue), [utils, valueManager, inputValue]);
  const setInputTimezone = (0, _useEventCallback.default)(newValue => {
    if (inputTimezone == null) {
      return newValue;
    }
    return valueManager.setTimezone(utils, inputTimezone, newValue);
  });
  const timezoneToRender = timezoneProp ?? inputTimezone ?? 'default';
  const valueWithTimezoneToRender = React.useMemo(() => valueManager.setTimezone(utils, timezoneToRender, inputValue), [valueManager, utils, timezoneToRender, inputValue]);
  const handleValueChange = (0, _useEventCallback.default)((newValue, ...otherParams) => {
    const newValueWithInputTimezone = setInputTimezone(newValue);
    onChange?.(newValueWithInputTimezone, ...otherParams);
  });
  return {
    value: valueWithTimezoneToRender,
    handleValueChange,
    timezone: timezoneToRender
  };
};

/**
 * Wrapper around `useControlled` and `useValueWithTimezone`
 */
exports.useValueWithTimezone = useValueWithTimezone;
const useControlledValueWithTimezone = ({
  name,
  timezone: timezoneProp,
  value: valueProp,
  defaultValue,
  onChange: onChangeProp,
  valueManager
}) => {
  const [valueWithInputTimezone, setValue] = (0, _useControlled.default)({
    name,
    state: 'value',
    controlled: valueProp,
    default: defaultValue ?? valueManager.emptyValue
  });
  const onChange = (0, _useEventCallback.default)((newValue, ...otherParams) => {
    setValue(newValue);
    onChangeProp?.(newValue, ...otherParams);
  });
  return useValueWithTimezone({
    timezone: timezoneProp,
    value: valueWithInputTimezone,
    defaultValue: undefined,
    onChange,
    valueManager
  });
};
exports.useControlledValueWithTimezone = useControlledValueWithTimezone;