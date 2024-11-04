"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useField = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var React = _interopRequireWildcard(require("react"));
var _useEnhancedEffect = _interopRequireDefault(require("@mui/utils/useEnhancedEffect"));
var _useEventCallback = _interopRequireDefault(require("@mui/utils/useEventCallback"));
var _RtlProvider = require("@mui/system/RtlProvider");
var _validation = require("../../../validation");
var _useUtils = require("../useUtils");
var _useField = require("./useField.utils");
var _useFieldState = require("./useFieldState");
var _useFieldCharacterEditing = require("./useFieldCharacterEditing");
var _useFieldV7TextField = require("./useFieldV7TextField");
var _useFieldV6TextField = require("./useFieldV6TextField");
const useField = params => {
  const utils = (0, _useUtils.useUtils)();
  const {
    internalProps,
    internalProps: {
      unstableFieldRef,
      minutesStep,
      enableAccessibleFieldDOMStructure = false,
      disabled = false,
      readOnly = false
    },
    forwardedProps: {
      onKeyDown,
      error,
      clearable,
      onClear
    },
    fieldValueManager,
    valueManager,
    validator
  } = params;
  const isRtl = (0, _RtlProvider.useRtl)();
  const stateResponse = (0, _useFieldState.useFieldState)(params);
  const {
    state,
    activeSectionIndex,
    parsedSelectedSections,
    setSelectedSections,
    clearValue,
    clearActiveSection,
    updateSectionValue,
    setTempAndroidValueStr,
    sectionsValueBoundaries,
    localizedDigits,
    timezone
  } = stateResponse;
  const characterEditingResponse = (0, _useFieldCharacterEditing.useFieldCharacterEditing)({
    sections: state.sections,
    updateSectionValue,
    sectionsValueBoundaries,
    localizedDigits,
    setTempAndroidValueStr,
    timezone
  });
  const {
    resetCharacterQuery
  } = characterEditingResponse;
  const areAllSectionsEmpty = valueManager.areValuesEqual(utils, state.value, valueManager.emptyValue);
  const useFieldTextField = enableAccessibleFieldDOMStructure ? _useFieldV7TextField.useFieldV7TextField : _useFieldV6TextField.useFieldV6TextField;
  const sectionOrder = React.useMemo(() => (0, _useField.getSectionOrder)(state.sections, isRtl && !enableAccessibleFieldDOMStructure), [state.sections, isRtl, enableAccessibleFieldDOMStructure]);
  const {
    returnedValue,
    interactions
  } = useFieldTextField((0, _extends2.default)({}, params, stateResponse, characterEditingResponse, {
    areAllSectionsEmpty,
    sectionOrder
  }));
  const handleContainerKeyDown = (0, _useEventCallback.default)(event => {
    onKeyDown?.(event);
    if (disabled) {
      return;
    }
    // eslint-disable-next-line default-case
    switch (true) {
      // Select all
      case (event.ctrlKey || event.metaKey) && String.fromCharCode(event.keyCode) === 'A' && !event.shiftKey && !event.altKey:
        {
          // prevent default to make sure that the next line "select all" while updating
          // the internal state at the same time.
          event.preventDefault();
          setSelectedSections('all');
          break;
        }

      // Move selection to next section
      case event.key === 'ArrowRight':
        {
          event.preventDefault();
          if (parsedSelectedSections == null) {
            setSelectedSections(sectionOrder.startIndex);
          } else if (parsedSelectedSections === 'all') {
            setSelectedSections(sectionOrder.endIndex);
          } else {
            const nextSectionIndex = sectionOrder.neighbors[parsedSelectedSections].rightIndex;
            if (nextSectionIndex !== null) {
              setSelectedSections(nextSectionIndex);
            }
          }
          break;
        }

      // Move selection to previous section
      case event.key === 'ArrowLeft':
        {
          event.preventDefault();
          if (parsedSelectedSections == null) {
            setSelectedSections(sectionOrder.endIndex);
          } else if (parsedSelectedSections === 'all') {
            setSelectedSections(sectionOrder.startIndex);
          } else {
            const nextSectionIndex = sectionOrder.neighbors[parsedSelectedSections].leftIndex;
            if (nextSectionIndex !== null) {
              setSelectedSections(nextSectionIndex);
            }
          }
          break;
        }

      // Reset the value of the selected section
      case event.key === 'Delete':
        {
          event.preventDefault();
          if (readOnly) {
            break;
          }
          if (parsedSelectedSections == null || parsedSelectedSections === 'all') {
            clearValue();
          } else {
            clearActiveSection();
          }
          resetCharacterQuery();
          break;
        }

      // Increment / decrement the selected section value
      case ['ArrowUp', 'ArrowDown', 'Home', 'End', 'PageUp', 'PageDown'].includes(event.key):
        {
          event.preventDefault();
          if (readOnly || activeSectionIndex == null) {
            break;
          }
          const activeSection = state.sections[activeSectionIndex];
          const activeDateManager = fieldValueManager.getActiveDateManager(utils, state, activeSection);
          const newSectionValue = (0, _useField.adjustSectionValue)(utils, timezone, activeSection, event.key, sectionsValueBoundaries, localizedDigits, activeDateManager.date, {
            minutesStep
          });
          updateSectionValue({
            activeSection,
            newSectionValue,
            shouldGoToNextSection: false
          });
          break;
        }
    }
  });
  (0, _useEnhancedEffect.default)(() => {
    interactions.syncSelectionToDOM();
  });
  const {
    hasValidationError
  } = (0, _validation.useValidation)({
    props: internalProps,
    validator,
    timezone,
    value: state.value,
    onError: internalProps.onError
  });
  const inputError = React.useMemo(() => {
    // only override when `error` is undefined.
    // in case of multi input fields, the `error` value is provided externally and will always be defined.
    if (error !== undefined) {
      return error;
    }
    return hasValidationError;
  }, [hasValidationError, error]);
  React.useEffect(() => {
    if (!inputError && activeSectionIndex == null) {
      resetCharacterQuery();
    }
  }, [state.referenceValue, activeSectionIndex, inputError]); // eslint-disable-line react-hooks/exhaustive-deps

  // If `tempValueStrAndroid` is still defined for some section when running `useEffect`,
  // Then `onChange` has only been called once, which means the user pressed `Backspace` to reset the section.
  // This causes a small flickering on Android,
  // But we can't use `useEnhancedEffect` which is always called before the second `onChange` call and then would cause false positives.
  React.useEffect(() => {
    if (state.tempValueStrAndroid != null && activeSectionIndex != null) {
      resetCharacterQuery();
      clearActiveSection();
    }
  }, [state.sections]); // eslint-disable-line react-hooks/exhaustive-deps

  React.useImperativeHandle(unstableFieldRef, () => ({
    getSections: () => state.sections,
    getActiveSectionIndex: interactions.getActiveSectionIndexFromDOM,
    setSelectedSections: interactions.setSelectedSections,
    focusField: interactions.focusField,
    isFieldFocused: interactions.isFieldFocused
  }));
  const handleClearValue = (0, _useEventCallback.default)((event, ...args) => {
    event.preventDefault();
    onClear?.(event, ...args);
    clearValue();
    if (!interactions.isFieldFocused()) {
      // setSelectedSections is called internally
      interactions.focusField(0);
    } else {
      setSelectedSections(sectionOrder.startIndex);
    }
  });
  const commonForwardedProps = {
    onKeyDown: handleContainerKeyDown,
    onClear: handleClearValue,
    error: inputError,
    clearable: Boolean(clearable && !areAllSectionsEmpty && !readOnly && !disabled)
  };
  const commonAdditionalProps = {
    disabled,
    readOnly
  };
  return (0, _extends2.default)({}, params.forwardedProps, commonForwardedProps, commonAdditionalProps, returnedValue);
};
exports.useField = useField;