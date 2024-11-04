"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.usePickerValue = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var React = _interopRequireWildcard(require("react"));
var _useEventCallback = _interopRequireDefault(require("@mui/utils/useEventCallback"));
var _useOpenState = require("../useOpenState");
var _useUtils = require("../useUtils");
var _validation = require("../../../validation");
var _useValueWithTimezone = require("../useValueWithTimezone");
/**
 * Decide if the new value should be published
 * The published value will be passed to `onChange` if defined.
 */
const shouldPublishValue = params => {
  const {
    action,
    hasChanged,
    dateState,
    isControlled
  } = params;
  const isCurrentValueTheDefaultValue = !isControlled && !dateState.hasBeenModifiedSinceMount;

  // The field is responsible for only calling `onChange` when needed.
  if (action.name === 'setValueFromField') {
    return true;
  }
  if (action.name === 'setValueFromAction') {
    // If the component is not controlled, and the value has not been modified since the mount,
    // Then we want to publish the default value whenever the user pressed the "Accept", "Today" or "Clear" button.
    if (isCurrentValueTheDefaultValue && ['accept', 'today', 'clear'].includes(action.pickerAction)) {
      return true;
    }
    return hasChanged(dateState.lastPublishedValue);
  }
  if (action.name === 'setValueFromView' && action.selectionState !== 'shallow') {
    // On the first view,
    // If the value is not controlled, then clicking on any value (including the one equal to `defaultValue`) should call `onChange`
    if (isCurrentValueTheDefaultValue) {
      return true;
    }
    return hasChanged(dateState.lastPublishedValue);
  }
  if (action.name === 'setValueFromShortcut') {
    // On the first view,
    // If the value is not controlled, then clicking on any value (including the one equal to `defaultValue`) should call `onChange`
    if (isCurrentValueTheDefaultValue) {
      return true;
    }
    return hasChanged(dateState.lastPublishedValue);
  }
  return false;
};

/**
 * Decide if the new value should be committed.
 * The committed value will be passed to `onAccept` if defined.
 * It will also be used as a reset target when calling the `cancel` picker action (when clicking on the "Cancel" button).
 */
const shouldCommitValue = params => {
  const {
    action,
    hasChanged,
    dateState,
    isControlled,
    closeOnSelect
  } = params;
  const isCurrentValueTheDefaultValue = !isControlled && !dateState.hasBeenModifiedSinceMount;
  if (action.name === 'setValueFromAction') {
    // If the component is not controlled, and the value has not been modified since the mount,
    // Then we want to commit the default value whenever the user pressed the "Accept", "Today" or "Clear" button.
    if (isCurrentValueTheDefaultValue && ['accept', 'today', 'clear'].includes(action.pickerAction)) {
      return true;
    }
    return hasChanged(dateState.lastCommittedValue);
  }
  if (action.name === 'setValueFromView' && action.selectionState === 'finish' && closeOnSelect) {
    // On picker where the 1st view is also the last view,
    // If the value is not controlled, then clicking on any value (including the one equal to `defaultValue`) should call `onAccept`
    if (isCurrentValueTheDefaultValue) {
      return true;
    }
    return hasChanged(dateState.lastCommittedValue);
  }
  if (action.name === 'setValueFromShortcut') {
    return action.changeImportance === 'accept' && hasChanged(dateState.lastCommittedValue);
  }
  return false;
};

/**
 * Decide if the picker should be closed after the value is updated.
 */
const shouldClosePicker = params => {
  const {
    action,
    closeOnSelect
  } = params;
  if (action.name === 'setValueFromAction') {
    return true;
  }
  if (action.name === 'setValueFromView') {
    return action.selectionState === 'finish' && closeOnSelect;
  }
  if (action.name === 'setValueFromShortcut') {
    return action.changeImportance === 'accept';
  }
  return false;
};

/**
 * Manage the value lifecycle of all the pickers.
 */
const usePickerValue = ({
  props,
  valueManager,
  valueType,
  wrapperVariant,
  validator
}) => {
  const {
    onAccept,
    onChange,
    value: inValueWithoutRenderTimezone,
    defaultValue: inDefaultValue,
    closeOnSelect = wrapperVariant === 'desktop',
    timezone: timezoneProp
  } = props;
  const {
    current: defaultValue
  } = React.useRef(inDefaultValue);
  const {
    current: isControlled
  } = React.useRef(inValueWithoutRenderTimezone !== undefined);

  /* eslint-disable react-hooks/rules-of-hooks, react-hooks/exhaustive-deps */
  if (process.env.NODE_ENV !== 'production') {
    React.useEffect(() => {
      if (isControlled !== (inValueWithoutRenderTimezone !== undefined)) {
        console.error([`MUI X: A component is changing the ${isControlled ? '' : 'un'}controlled value of a picker to be ${isControlled ? 'un' : ''}controlled.`, 'Elements should not switch from uncontrolled to controlled (or vice versa).', `Decide between using a controlled or uncontrolled value` + 'for the lifetime of the component.', "The nature of the state is determined during the first render. It's considered controlled if the value is not `undefined`.", 'More info: https://fb.me/react-controlled-components'].join('\n'));
      }
    }, [inValueWithoutRenderTimezone]);
    React.useEffect(() => {
      if (!isControlled && defaultValue !== inDefaultValue) {
        console.error([`MUI X: A component is changing the defaultValue of an uncontrolled picker after being initialized. ` + `To suppress this warning opt to use a controlled value.`].join('\n'));
      }
    }, [JSON.stringify(defaultValue)]);
  }
  /* eslint-enable react-hooks/rules-of-hooks, react-hooks/exhaustive-deps */

  const utils = (0, _useUtils.useUtils)();
  const adapter = (0, _useUtils.useLocalizationContext)();
  const {
    isOpen,
    setIsOpen
  } = (0, _useOpenState.useOpenState)(props);
  const {
    timezone,
    value: inValueWithTimezoneToRender,
    handleValueChange
  } = (0, _useValueWithTimezone.useValueWithTimezone)({
    timezone: timezoneProp,
    value: inValueWithoutRenderTimezone,
    defaultValue,
    onChange,
    valueManager
  });
  const [dateState, setDateState] = React.useState(() => {
    let initialValue;
    if (inValueWithTimezoneToRender !== undefined) {
      initialValue = inValueWithTimezoneToRender;
    } else if (defaultValue !== undefined) {
      initialValue = defaultValue;
    } else {
      initialValue = valueManager.emptyValue;
    }
    return {
      draft: initialValue,
      lastPublishedValue: initialValue,
      lastCommittedValue: initialValue,
      lastControlledValue: inValueWithTimezoneToRender,
      hasBeenModifiedSinceMount: false
    };
  });
  const {
    getValidationErrorForNewValue
  } = (0, _validation.useValidation)({
    props,
    validator,
    timezone,
    value: dateState.draft,
    onError: props.onError
  });
  const updateDate = (0, _useEventCallback.default)(action => {
    const updaterParams = {
      action,
      dateState,
      hasChanged: comparison => !valueManager.areValuesEqual(utils, action.value, comparison),
      isControlled,
      closeOnSelect
    };
    const shouldPublish = shouldPublishValue(updaterParams);
    const shouldCommit = shouldCommitValue(updaterParams);
    const shouldClose = shouldClosePicker(updaterParams);
    setDateState(prev => (0, _extends2.default)({}, prev, {
      draft: action.value,
      lastPublishedValue: shouldPublish ? action.value : prev.lastPublishedValue,
      lastCommittedValue: shouldCommit ? action.value : prev.lastCommittedValue,
      hasBeenModifiedSinceMount: true
    }));
    let cachedContext = null;
    const getContext = () => {
      if (!cachedContext) {
        const validationError = action.name === 'setValueFromField' ? action.context.validationError : getValidationErrorForNewValue(action.value);
        cachedContext = {
          validationError
        };
        if (action.name === 'setValueFromShortcut') {
          cachedContext.shortcut = action.shortcut;
        }
      }
      return cachedContext;
    };
    if (shouldPublish) {
      handleValueChange(action.value, getContext());
    }
    if (shouldCommit && onAccept) {
      onAccept(action.value, getContext());
    }
    if (shouldClose) {
      setIsOpen(false);
    }
  });
  if (inValueWithTimezoneToRender !== undefined && (dateState.lastControlledValue === undefined || !valueManager.areValuesEqual(utils, dateState.lastControlledValue, inValueWithTimezoneToRender))) {
    const isUpdateComingFromPicker = valueManager.areValuesEqual(utils, dateState.draft, inValueWithTimezoneToRender);
    setDateState(prev => (0, _extends2.default)({}, prev, {
      lastControlledValue: inValueWithTimezoneToRender
    }, isUpdateComingFromPicker ? {} : {
      lastCommittedValue: inValueWithTimezoneToRender,
      lastPublishedValue: inValueWithTimezoneToRender,
      draft: inValueWithTimezoneToRender,
      hasBeenModifiedSinceMount: true
    }));
  }
  const handleClear = (0, _useEventCallback.default)(() => {
    updateDate({
      value: valueManager.emptyValue,
      name: 'setValueFromAction',
      pickerAction: 'clear'
    });
  });
  const handleAccept = (0, _useEventCallback.default)(() => {
    updateDate({
      value: dateState.lastPublishedValue,
      name: 'setValueFromAction',
      pickerAction: 'accept'
    });
  });
  const handleDismiss = (0, _useEventCallback.default)(() => {
    updateDate({
      value: dateState.lastPublishedValue,
      name: 'setValueFromAction',
      pickerAction: 'dismiss'
    });
  });
  const handleCancel = (0, _useEventCallback.default)(() => {
    updateDate({
      value: dateState.lastCommittedValue,
      name: 'setValueFromAction',
      pickerAction: 'cancel'
    });
  });
  const handleSetToday = (0, _useEventCallback.default)(() => {
    updateDate({
      value: valueManager.getTodayValue(utils, timezone, valueType),
      name: 'setValueFromAction',
      pickerAction: 'today'
    });
  });
  const handleOpen = (0, _useEventCallback.default)(event => {
    event.preventDefault();
    setIsOpen(true);
  });
  const handleClose = (0, _useEventCallback.default)(event => {
    event?.preventDefault();
    setIsOpen(false);
  });
  const handleChange = (0, _useEventCallback.default)((newValue, selectionState = 'partial') => updateDate({
    name: 'setValueFromView',
    value: newValue,
    selectionState
  }));
  const handleSelectShortcut = (0, _useEventCallback.default)((newValue, changeImportance, shortcut) => updateDate({
    name: 'setValueFromShortcut',
    value: newValue,
    changeImportance,
    shortcut
  }));
  const handleChangeFromField = (0, _useEventCallback.default)((newValue, context) => updateDate({
    name: 'setValueFromField',
    value: newValue,
    context
  }));
  const actions = {
    onClear: handleClear,
    onAccept: handleAccept,
    onDismiss: handleDismiss,
    onCancel: handleCancel,
    onSetToday: handleSetToday,
    onOpen: handleOpen,
    onClose: handleClose
  };
  const fieldResponse = {
    value: dateState.draft,
    onChange: handleChangeFromField
  };
  const viewValue = React.useMemo(() => valueManager.cleanValue(utils, dateState.draft), [utils, valueManager, dateState.draft]);
  const viewResponse = {
    value: viewValue,
    onChange: handleChange,
    onClose: handleClose,
    open: isOpen
  };
  const isValid = testedValue => {
    const error = validator({
      adapter,
      value: testedValue,
      timezone,
      props
    });
    return !valueManager.hasError(error);
  };
  const layoutResponse = (0, _extends2.default)({}, actions, {
    value: viewValue,
    onChange: handleChange,
    onSelectShortcut: handleSelectShortcut,
    isValid
  });
  const contextValue = React.useMemo(() => ({
    onOpen: handleOpen,
    onClose: handleClose,
    open: isOpen
  }), [isOpen, handleClose, handleOpen]);
  return {
    open: isOpen,
    fieldProps: fieldResponse,
    viewProps: viewResponse,
    layoutProps: layoutResponse,
    actions,
    contextValue
  };
};
exports.usePickerValue = usePickerValue;