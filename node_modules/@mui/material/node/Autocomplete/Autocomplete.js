"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
Object.defineProperty(exports, "createFilterOptions", {
  enumerable: true,
  get: function () {
    return _useAutocomplete.createFilterOptions;
  }
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _integerPropType = _interopRequireDefault(require("@mui/utils/integerPropType"));
var _chainPropTypes = _interopRequireDefault(require("@mui/utils/chainPropTypes"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _colorManipulator = require("@mui/system/colorManipulator");
var _useAutocomplete = _interopRequireWildcard(require("../useAutocomplete"));
var _Popper = _interopRequireDefault(require("../Popper"));
var _ListSubheader = _interopRequireDefault(require("../ListSubheader"));
var _Paper = _interopRequireDefault(require("../Paper"));
var _IconButton = _interopRequireDefault(require("../IconButton"));
var _Chip = _interopRequireDefault(require("../Chip"));
var _inputClasses = _interopRequireDefault(require("../Input/inputClasses"));
var _inputBaseClasses = _interopRequireDefault(require("../InputBase/inputBaseClasses"));
var _outlinedInputClasses = _interopRequireDefault(require("../OutlinedInput/outlinedInputClasses"));
var _filledInputClasses = _interopRequireDefault(require("../FilledInput/filledInputClasses"));
var _Close = _interopRequireDefault(require("../internal/svg-icons/Close"));
var _ArrowDropDown = _interopRequireDefault(require("../internal/svg-icons/ArrowDropDown"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _autocompleteClasses = _interopRequireWildcard(require("./autocompleteClasses"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _useSlot = _interopRequireDefault(require("../utils/useSlot"));
var _jsxRuntime = require("react/jsx-runtime");
var _ClearIcon, _ArrowDropDownIcon;
const useUtilityClasses = ownerState => {
  const {
    classes,
    disablePortal,
    expanded,
    focused,
    fullWidth,
    hasClearIcon,
    hasPopupIcon,
    inputFocused,
    popupOpen,
    size
  } = ownerState;
  const slots = {
    root: ['root', expanded && 'expanded', focused && 'focused', fullWidth && 'fullWidth', hasClearIcon && 'hasClearIcon', hasPopupIcon && 'hasPopupIcon'],
    inputRoot: ['inputRoot'],
    input: ['input', inputFocused && 'inputFocused'],
    tag: ['tag', `tagSize${(0, _capitalize.default)(size)}`],
    endAdornment: ['endAdornment'],
    clearIndicator: ['clearIndicator'],
    popupIndicator: ['popupIndicator', popupOpen && 'popupIndicatorOpen'],
    popper: ['popper', disablePortal && 'popperDisablePortal'],
    paper: ['paper'],
    listbox: ['listbox'],
    loading: ['loading'],
    noOptions: ['noOptions'],
    option: ['option'],
    groupLabel: ['groupLabel'],
    groupUl: ['groupUl']
  };
  return (0, _composeClasses.default)(slots, _autocompleteClasses.getAutocompleteUtilityClass, classes);
};
const AutocompleteRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiAutocomplete',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    const {
      fullWidth,
      hasClearIcon,
      hasPopupIcon,
      inputFocused,
      size
    } = ownerState;
    return [{
      [`& .${_autocompleteClasses.default.tag}`]: styles.tag
    }, {
      [`& .${_autocompleteClasses.default.tag}`]: styles[`tagSize${(0, _capitalize.default)(size)}`]
    }, {
      [`& .${_autocompleteClasses.default.inputRoot}`]: styles.inputRoot
    }, {
      [`& .${_autocompleteClasses.default.input}`]: styles.input
    }, {
      [`& .${_autocompleteClasses.default.input}`]: inputFocused && styles.inputFocused
    }, styles.root, fullWidth && styles.fullWidth, hasPopupIcon && styles.hasPopupIcon, hasClearIcon && styles.hasClearIcon];
  }
})({
  [`&.${_autocompleteClasses.default.focused} .${_autocompleteClasses.default.clearIndicator}`]: {
    visibility: 'visible'
  },
  /* Avoid double tap issue on iOS */
  '@media (pointer: fine)': {
    [`&:hover .${_autocompleteClasses.default.clearIndicator}`]: {
      visibility: 'visible'
    }
  },
  [`& .${_autocompleteClasses.default.tag}`]: {
    margin: 3,
    maxWidth: 'calc(100% - 6px)'
  },
  [`& .${_autocompleteClasses.default.inputRoot}`]: {
    [`.${_autocompleteClasses.default.hasPopupIcon}&, .${_autocompleteClasses.default.hasClearIcon}&`]: {
      paddingRight: 26 + 4
    },
    [`.${_autocompleteClasses.default.hasPopupIcon}.${_autocompleteClasses.default.hasClearIcon}&`]: {
      paddingRight: 52 + 4
    },
    [`& .${_autocompleteClasses.default.input}`]: {
      width: 0,
      minWidth: 30
    }
  },
  [`& .${_inputClasses.default.root}`]: {
    paddingBottom: 1,
    '& .MuiInput-input': {
      padding: '4px 4px 4px 0px'
    }
  },
  [`& .${_inputClasses.default.root}.${_inputBaseClasses.default.sizeSmall}`]: {
    [`& .${_inputClasses.default.input}`]: {
      padding: '2px 4px 3px 0'
    }
  },
  [`& .${_outlinedInputClasses.default.root}`]: {
    padding: 9,
    [`.${_autocompleteClasses.default.hasPopupIcon}&, .${_autocompleteClasses.default.hasClearIcon}&`]: {
      paddingRight: 26 + 4 + 9
    },
    [`.${_autocompleteClasses.default.hasPopupIcon}.${_autocompleteClasses.default.hasClearIcon}&`]: {
      paddingRight: 52 + 4 + 9
    },
    [`& .${_autocompleteClasses.default.input}`]: {
      padding: '7.5px 4px 7.5px 5px'
    },
    [`& .${_autocompleteClasses.default.endAdornment}`]: {
      right: 9
    }
  },
  [`& .${_outlinedInputClasses.default.root}.${_inputBaseClasses.default.sizeSmall}`]: {
    // Don't specify paddingRight, as it overrides the default value set when there is only
    // one of the popup or clear icon as the specificity is equal so the latter one wins
    paddingTop: 6,
    paddingBottom: 6,
    paddingLeft: 6,
    [`& .${_autocompleteClasses.default.input}`]: {
      padding: '2.5px 4px 2.5px 8px'
    }
  },
  [`& .${_filledInputClasses.default.root}`]: {
    paddingTop: 19,
    paddingLeft: 8,
    [`.${_autocompleteClasses.default.hasPopupIcon}&, .${_autocompleteClasses.default.hasClearIcon}&`]: {
      paddingRight: 26 + 4 + 9
    },
    [`.${_autocompleteClasses.default.hasPopupIcon}.${_autocompleteClasses.default.hasClearIcon}&`]: {
      paddingRight: 52 + 4 + 9
    },
    [`& .${_filledInputClasses.default.input}`]: {
      padding: '7px 4px'
    },
    [`& .${_autocompleteClasses.default.endAdornment}`]: {
      right: 9
    }
  },
  [`& .${_filledInputClasses.default.root}.${_inputBaseClasses.default.sizeSmall}`]: {
    paddingBottom: 1,
    [`& .${_filledInputClasses.default.input}`]: {
      padding: '2.5px 4px'
    }
  },
  [`& .${_inputBaseClasses.default.hiddenLabel}`]: {
    paddingTop: 8
  },
  [`& .${_filledInputClasses.default.root}.${_inputBaseClasses.default.hiddenLabel}`]: {
    paddingTop: 0,
    paddingBottom: 0,
    [`& .${_autocompleteClasses.default.input}`]: {
      paddingTop: 16,
      paddingBottom: 17
    }
  },
  [`& .${_filledInputClasses.default.root}.${_inputBaseClasses.default.hiddenLabel}.${_inputBaseClasses.default.sizeSmall}`]: {
    [`& .${_autocompleteClasses.default.input}`]: {
      paddingTop: 8,
      paddingBottom: 9
    }
  },
  [`& .${_autocompleteClasses.default.input}`]: {
    flexGrow: 1,
    textOverflow: 'ellipsis',
    opacity: 0
  },
  variants: [{
    props: {
      fullWidth: true
    },
    style: {
      width: '100%'
    }
  }, {
    props: {
      size: 'small'
    },
    style: {
      [`& .${_autocompleteClasses.default.tag}`]: {
        margin: 2,
        maxWidth: 'calc(100% - 4px)'
      }
    }
  }, {
    props: {
      inputFocused: true
    },
    style: {
      [`& .${_autocompleteClasses.default.input}`]: {
        opacity: 1
      }
    }
  }, {
    props: {
      multiple: true
    },
    style: {
      [`& .${_autocompleteClasses.default.inputRoot}`]: {
        flexWrap: 'wrap'
      }
    }
  }]
});
const AutocompleteEndAdornment = (0, _zeroStyled.styled)('div', {
  name: 'MuiAutocomplete',
  slot: 'EndAdornment',
  overridesResolver: (props, styles) => styles.endAdornment
})({
  // We use a position absolute to support wrapping tags.
  position: 'absolute',
  right: 0,
  top: '50%',
  transform: 'translate(0, -50%)'
});
const AutocompleteClearIndicator = (0, _zeroStyled.styled)(_IconButton.default, {
  name: 'MuiAutocomplete',
  slot: 'ClearIndicator',
  overridesResolver: (props, styles) => styles.clearIndicator
})({
  marginRight: -2,
  padding: 4,
  visibility: 'hidden'
});
const AutocompletePopupIndicator = (0, _zeroStyled.styled)(_IconButton.default, {
  name: 'MuiAutocomplete',
  slot: 'PopupIndicator',
  overridesResolver: ({
    ownerState
  }, styles) => ({
    ...styles.popupIndicator,
    ...(ownerState.popupOpen && styles.popupIndicatorOpen)
  })
})({
  padding: 2,
  marginRight: -2,
  variants: [{
    props: {
      popupOpen: true
    },
    style: {
      transform: 'rotate(180deg)'
    }
  }]
});
const AutocompletePopper = (0, _zeroStyled.styled)(_Popper.default, {
  name: 'MuiAutocomplete',
  slot: 'Popper',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [{
      [`& .${_autocompleteClasses.default.option}`]: styles.option
    }, styles.popper, ownerState.disablePortal && styles.popperDisablePortal];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  zIndex: (theme.vars || theme).zIndex.modal,
  variants: [{
    props: {
      disablePortal: true
    },
    style: {
      position: 'absolute'
    }
  }]
})));
const AutocompletePaper = (0, _zeroStyled.styled)(_Paper.default, {
  name: 'MuiAutocomplete',
  slot: 'Paper',
  overridesResolver: (props, styles) => styles.paper
})((0, _memoTheme.default)(({
  theme
}) => ({
  ...theme.typography.body1,
  overflow: 'auto'
})));
const AutocompleteLoading = (0, _zeroStyled.styled)('div', {
  name: 'MuiAutocomplete',
  slot: 'Loading',
  overridesResolver: (props, styles) => styles.loading
})((0, _memoTheme.default)(({
  theme
}) => ({
  color: (theme.vars || theme).palette.text.secondary,
  padding: '14px 16px'
})));
const AutocompleteNoOptions = (0, _zeroStyled.styled)('div', {
  name: 'MuiAutocomplete',
  slot: 'NoOptions',
  overridesResolver: (props, styles) => styles.noOptions
})((0, _memoTheme.default)(({
  theme
}) => ({
  color: (theme.vars || theme).palette.text.secondary,
  padding: '14px 16px'
})));
const AutocompleteListbox = (0, _zeroStyled.styled)('div', {
  name: 'MuiAutocomplete',
  slot: 'Listbox',
  overridesResolver: (props, styles) => styles.listbox
})((0, _memoTheme.default)(({
  theme
}) => ({
  listStyle: 'none',
  margin: 0,
  padding: '8px 0',
  maxHeight: '40vh',
  overflow: 'auto',
  position: 'relative',
  [`& .${_autocompleteClasses.default.option}`]: {
    minHeight: 48,
    display: 'flex',
    overflow: 'hidden',
    justifyContent: 'flex-start',
    alignItems: 'center',
    cursor: 'pointer',
    paddingTop: 6,
    boxSizing: 'border-box',
    outline: '0',
    WebkitTapHighlightColor: 'transparent',
    paddingBottom: 6,
    paddingLeft: 16,
    paddingRight: 16,
    [theme.breakpoints.up('sm')]: {
      minHeight: 'auto'
    },
    [`&.${_autocompleteClasses.default.focused}`]: {
      backgroundColor: (theme.vars || theme).palette.action.hover,
      // Reset on touch devices, it doesn't add specificity
      '@media (hover: none)': {
        backgroundColor: 'transparent'
      }
    },
    '&[aria-disabled="true"]': {
      opacity: (theme.vars || theme).palette.action.disabledOpacity,
      pointerEvents: 'none'
    },
    [`&.${_autocompleteClasses.default.focusVisible}`]: {
      backgroundColor: (theme.vars || theme).palette.action.focus
    },
    '&[aria-selected="true"]': {
      backgroundColor: theme.vars ? `rgba(${theme.vars.palette.primary.mainChannel} / ${theme.vars.palette.action.selectedOpacity})` : (0, _colorManipulator.alpha)(theme.palette.primary.main, theme.palette.action.selectedOpacity),
      [`&.${_autocompleteClasses.default.focused}`]: {
        backgroundColor: theme.vars ? `rgba(${theme.vars.palette.primary.mainChannel} / calc(${theme.vars.palette.action.selectedOpacity} + ${theme.vars.palette.action.hoverOpacity}))` : (0, _colorManipulator.alpha)(theme.palette.primary.main, theme.palette.action.selectedOpacity + theme.palette.action.hoverOpacity),
        // Reset on touch devices, it doesn't add specificity
        '@media (hover: none)': {
          backgroundColor: (theme.vars || theme).palette.action.selected
        }
      },
      [`&.${_autocompleteClasses.default.focusVisible}`]: {
        backgroundColor: theme.vars ? `rgba(${theme.vars.palette.primary.mainChannel} / calc(${theme.vars.palette.action.selectedOpacity} + ${theme.vars.palette.action.focusOpacity}))` : (0, _colorManipulator.alpha)(theme.palette.primary.main, theme.palette.action.selectedOpacity + theme.palette.action.focusOpacity)
      }
    }
  }
})));
const AutocompleteGroupLabel = (0, _zeroStyled.styled)(_ListSubheader.default, {
  name: 'MuiAutocomplete',
  slot: 'GroupLabel',
  overridesResolver: (props, styles) => styles.groupLabel
})((0, _memoTheme.default)(({
  theme
}) => ({
  backgroundColor: (theme.vars || theme).palette.background.paper,
  top: -8
})));
const AutocompleteGroupUl = (0, _zeroStyled.styled)('ul', {
  name: 'MuiAutocomplete',
  slot: 'GroupUl',
  overridesResolver: (props, styles) => styles.groupUl
})({
  padding: 0,
  [`& .${_autocompleteClasses.default.option}`]: {
    paddingLeft: 24
  }
});
const Autocomplete = /*#__PURE__*/React.forwardRef(function Autocomplete(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiAutocomplete'
  });

  /* eslint-disable @typescript-eslint/no-unused-vars */
  const {
    autoComplete = false,
    autoHighlight = false,
    autoSelect = false,
    blurOnSelect = false,
    ChipProps: ChipPropsProp,
    className,
    clearIcon = _ClearIcon || (_ClearIcon = /*#__PURE__*/(0, _jsxRuntime.jsx)(_Close.default, {
      fontSize: "small"
    })),
    clearOnBlur = !props.freeSolo,
    clearOnEscape = false,
    clearText = 'Clear',
    closeText = 'Close',
    componentsProps,
    defaultValue = props.multiple ? [] : null,
    disableClearable = false,
    disableCloseOnSelect = false,
    disabled = false,
    disabledItemsFocusable = false,
    disableListWrap = false,
    disablePortal = false,
    filterOptions,
    filterSelectedOptions = false,
    forcePopupIcon = 'auto',
    freeSolo = false,
    fullWidth = false,
    getLimitTagsText = more => `+${more}`,
    getOptionDisabled,
    getOptionKey,
    getOptionLabel: getOptionLabelProp,
    isOptionEqualToValue,
    groupBy,
    handleHomeEndKeys = !props.freeSolo,
    id: idProp,
    includeInputInList = false,
    inputValue: inputValueProp,
    limitTags = -1,
    ListboxComponent: ListboxComponentProp,
    ListboxProps: ListboxPropsProp,
    loading = false,
    loadingText = 'Loading…',
    multiple = false,
    noOptionsText = 'No options',
    onChange,
    onClose,
    onHighlightChange,
    onInputChange,
    onOpen,
    open,
    openOnFocus = false,
    openText = 'Open',
    options,
    PaperComponent: PaperComponentProp,
    PopperComponent: PopperComponentProp,
    popupIcon = _ArrowDropDownIcon || (_ArrowDropDownIcon = /*#__PURE__*/(0, _jsxRuntime.jsx)(_ArrowDropDown.default, {})),
    readOnly = false,
    renderGroup: renderGroupProp,
    renderInput,
    renderOption: renderOptionProp,
    renderTags,
    selectOnFocus = !props.freeSolo,
    size = 'medium',
    slots = {},
    slotProps = {},
    value: valueProp,
    ...other
  } = props;
  /* eslint-enable @typescript-eslint/no-unused-vars */

  const {
    getRootProps,
    getInputProps,
    getInputLabelProps,
    getPopupIndicatorProps,
    getClearProps,
    getTagProps,
    getListboxProps,
    getOptionProps,
    value,
    dirty,
    expanded,
    id,
    popupOpen,
    focused,
    focusedTag,
    anchorEl,
    setAnchorEl,
    inputValue,
    groupedOptions
  } = (0, _useAutocomplete.default)({
    ...props,
    componentName: 'Autocomplete'
  });
  const hasClearIcon = !disableClearable && !disabled && dirty && !readOnly;
  const hasPopupIcon = (!freeSolo || forcePopupIcon === true) && forcePopupIcon !== false;
  const {
    onMouseDown: handleInputMouseDown
  } = getInputProps();
  const {
    ref: listboxRef,
    ...otherListboxProps
  } = getListboxProps();
  const defaultGetOptionLabel = option => option.label ?? option;
  const getOptionLabel = getOptionLabelProp || defaultGetOptionLabel;

  // If you modify this, make sure to keep the `AutocompleteOwnerState` type in sync.
  const ownerState = {
    ...props,
    disablePortal,
    expanded,
    focused,
    fullWidth,
    getOptionLabel,
    hasClearIcon,
    hasPopupIcon,
    inputFocused: focusedTag === -1,
    popupOpen,
    size
  };
  const classes = useUtilityClasses(ownerState);
  const externalForwardedProps = {
    slots: {
      paper: PaperComponentProp,
      popper: PopperComponentProp,
      ...slots
    },
    slotProps: {
      chip: ChipPropsProp,
      listbox: ListboxPropsProp,
      ...componentsProps,
      ...slotProps
    }
  };
  const [ListboxSlot, listboxProps] = (0, _useSlot.default)('listbox', {
    elementType: AutocompleteListbox,
    externalForwardedProps,
    ownerState,
    className: classes.listbox,
    additionalProps: otherListboxProps,
    ref: listboxRef
  });
  const [PaperSlot, paperProps] = (0, _useSlot.default)('paper', {
    elementType: _Paper.default,
    externalForwardedProps,
    ownerState,
    className: classes.paper
  });
  const [PopperSlot, popperProps] = (0, _useSlot.default)('popper', {
    elementType: _Popper.default,
    externalForwardedProps,
    ownerState,
    className: classes.popper,
    additionalProps: {
      disablePortal,
      style: {
        width: anchorEl ? anchorEl.clientWidth : null
      },
      role: 'presentation',
      anchorEl,
      open: popupOpen
    }
  });
  let startAdornment;
  if (multiple && value.length > 0) {
    const getCustomizedTagProps = params => ({
      className: classes.tag,
      disabled,
      ...getTagProps(params)
    });
    if (renderTags) {
      startAdornment = renderTags(value, getCustomizedTagProps, ownerState);
    } else {
      startAdornment = value.map((option, index) => {
        const {
          key,
          ...customTagProps
        } = getCustomizedTagProps({
          index
        });
        return /*#__PURE__*/(0, _jsxRuntime.jsx)(_Chip.default, {
          label: getOptionLabel(option),
          size: size,
          ...customTagProps,
          ...externalForwardedProps.slotProps.chip
        }, key);
      });
    }
  }
  if (limitTags > -1 && Array.isArray(startAdornment)) {
    const more = startAdornment.length - limitTags;
    if (!focused && more > 0) {
      startAdornment = startAdornment.splice(0, limitTags);
      startAdornment.push(/*#__PURE__*/(0, _jsxRuntime.jsx)("span", {
        className: classes.tag,
        children: getLimitTagsText(more)
      }, startAdornment.length));
    }
  }
  const defaultRenderGroup = params => /*#__PURE__*/(0, _jsxRuntime.jsxs)("li", {
    children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(AutocompleteGroupLabel, {
      className: classes.groupLabel,
      ownerState: ownerState,
      component: "div",
      children: params.group
    }), /*#__PURE__*/(0, _jsxRuntime.jsx)(AutocompleteGroupUl, {
      className: classes.groupUl,
      ownerState: ownerState,
      children: params.children
    })]
  }, params.key);
  const renderGroup = renderGroupProp || defaultRenderGroup;
  const defaultRenderOption = (props2, option) => {
    // Need to clearly apply key because of https://github.com/vercel/next.js/issues/55642
    const {
      key,
      ...otherProps
    } = props2;
    return /*#__PURE__*/(0, _jsxRuntime.jsx)("li", {
      ...otherProps,
      children: getOptionLabel(option)
    }, key);
  };
  const renderOption = renderOptionProp || defaultRenderOption;
  const renderListOption = (option, index) => {
    const optionProps = getOptionProps({
      option,
      index
    });
    return renderOption({
      ...optionProps,
      className: classes.option
    }, option, {
      selected: optionProps['aria-selected'],
      index,
      inputValue
    }, ownerState);
  };
  const clearIndicatorSlotProps = externalForwardedProps.slotProps.clearIndicator;
  const popupIndicatorSlotProps = externalForwardedProps.slotProps.popupIndicator;
  const renderAutocompletePopperChildren = children => /*#__PURE__*/(0, _jsxRuntime.jsx)(AutocompletePopper, {
    as: PopperSlot,
    ...popperProps,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(AutocompletePaper, {
      as: PaperSlot,
      ...paperProps,
      children: children
    })
  });
  let autocompletePopper = null;
  if (groupedOptions.length > 0) {
    autocompletePopper = renderAutocompletePopperChildren(
    /*#__PURE__*/
    // TODO v7: remove `as` prop and move ListboxComponentProp to externalForwardedProps or remove ListboxComponentProp entirely
    // https://github.com/mui/material-ui/pull/43994#issuecomment-2401945800
    (0, _jsxRuntime.jsx)(ListboxSlot, {
      as: ListboxComponentProp,
      ...listboxProps,
      children: groupedOptions.map((option, index) => {
        if (groupBy) {
          return renderGroup({
            key: option.key,
            group: option.group,
            children: option.options.map((option2, index2) => renderListOption(option2, option.index + index2))
          });
        }
        return renderListOption(option, index);
      })
    }));
  } else if (loading && groupedOptions.length === 0) {
    autocompletePopper = renderAutocompletePopperChildren(/*#__PURE__*/(0, _jsxRuntime.jsx)(AutocompleteLoading, {
      className: classes.loading,
      ownerState: ownerState,
      children: loadingText
    }));
  } else if (groupedOptions.length === 0 && !freeSolo && !loading) {
    autocompletePopper = renderAutocompletePopperChildren(/*#__PURE__*/(0, _jsxRuntime.jsx)(AutocompleteNoOptions, {
      className: classes.noOptions,
      ownerState: ownerState,
      role: "presentation",
      onMouseDown: event => {
        // Prevent input blur when interacting with the "no options" content
        event.preventDefault();
      },
      children: noOptionsText
    }));
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(React.Fragment, {
    children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(AutocompleteRoot, {
      ref: ref,
      className: (0, _clsx.default)(classes.root, className),
      ownerState: ownerState,
      ...getRootProps(other),
      children: renderInput({
        id,
        disabled,
        fullWidth: true,
        size: size === 'small' ? 'small' : undefined,
        InputLabelProps: getInputLabelProps(),
        InputProps: {
          ref: setAnchorEl,
          className: classes.inputRoot,
          startAdornment,
          onMouseDown: event => {
            if (event.target === event.currentTarget) {
              handleInputMouseDown(event);
            }
          },
          ...((hasClearIcon || hasPopupIcon) && {
            endAdornment: /*#__PURE__*/(0, _jsxRuntime.jsxs)(AutocompleteEndAdornment, {
              className: classes.endAdornment,
              ownerState: ownerState,
              children: [hasClearIcon ? /*#__PURE__*/(0, _jsxRuntime.jsx)(AutocompleteClearIndicator, {
                ...getClearProps(),
                "aria-label": clearText,
                title: clearText,
                ownerState: ownerState,
                ...clearIndicatorSlotProps,
                className: (0, _clsx.default)(classes.clearIndicator, clearIndicatorSlotProps?.className),
                children: clearIcon
              }) : null, hasPopupIcon ? /*#__PURE__*/(0, _jsxRuntime.jsx)(AutocompletePopupIndicator, {
                ...getPopupIndicatorProps(),
                disabled: disabled,
                "aria-label": popupOpen ? closeText : openText,
                title: popupOpen ? closeText : openText,
                ownerState: ownerState,
                ...popupIndicatorSlotProps,
                className: (0, _clsx.default)(classes.popupIndicator, popupIndicatorSlotProps?.className),
                children: popupIcon
              }) : null]
            })
          })
        },
        inputProps: {
          className: classes.input,
          disabled,
          readOnly,
          ...getInputProps()
        }
      })
    }), anchorEl ? autocompletePopper : null]
  });
});
process.env.NODE_ENV !== "production" ? Autocomplete.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * If `true`, the portion of the selected suggestion that the user hasn't typed,
   * known as the completion string, appears inline after the input cursor in the textbox.
   * The inline completion string is visually highlighted and has a selected state.
   * @default false
   */
  autoComplete: _propTypes.default.bool,
  /**
   * If `true`, the first option is automatically highlighted.
   * @default false
   */
  autoHighlight: _propTypes.default.bool,
  /**
   * If `true`, the selected option becomes the value of the input
   * when the Autocomplete loses focus unless the user chooses
   * a different option or changes the character string in the input.
   *
   * When using the `freeSolo` mode, the typed value will be the input value
   * if the Autocomplete loses focus without highlighting an option.
   * @default false
   */
  autoSelect: _propTypes.default.bool,
  /**
   * Control if the input should be blurred when an option is selected:
   *
   * - `false` the input is not blurred.
   * - `true` the input is always blurred.
   * - `touch` the input is blurred after a touch event.
   * - `mouse` the input is blurred after a mouse event.
   * @default false
   */
  blurOnSelect: _propTypes.default.oneOfType([_propTypes.default.oneOf(['mouse', 'touch']), _propTypes.default.bool]),
  /**
   * Props applied to the [`Chip`](https://mui.com/material-ui/api/chip/) element.
   */
  ChipProps: _propTypes.default.object,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The icon to display in place of the default clear icon.
   * @default <ClearIcon fontSize="small" />
   */
  clearIcon: _propTypes.default.node,
  /**
   * If `true`, the input's text is cleared on blur if no value is selected.
   *
   * Set it to `true` if you want to help the user enter a new value.
   * Set it to `false` if you want to help the user resume their search.
   * @default !props.freeSolo
   */
  clearOnBlur: _propTypes.default.bool,
  /**
   * If `true`, clear all values when the user presses escape and the popup is closed.
   * @default false
   */
  clearOnEscape: _propTypes.default.bool,
  /**
   * Override the default text for the *clear* icon button.
   *
   * For localization purposes, you can use the provided [translations](https://mui.com/material-ui/guides/localization/).
   * @default 'Clear'
   */
  clearText: _propTypes.default.string,
  /**
   * Override the default text for the *close popup* icon button.
   *
   * For localization purposes, you can use the provided [translations](https://mui.com/material-ui/guides/localization/).
   * @default 'Close'
   */
  closeText: _propTypes.default.string,
  /**
   * The props used for each slot inside.
   * @deprecated Use the `slotProps` prop instead. This prop will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
   */
  componentsProps: _propTypes.default.shape({
    clearIndicator: _propTypes.default.object,
    paper: _propTypes.default.object,
    popper: _propTypes.default.object,
    popupIndicator: _propTypes.default.object
  }),
  /**
   * The default value. Use when the component is not controlled.
   * @default props.multiple ? [] : null
   */
  defaultValue: (0, _chainPropTypes.default)(_propTypes.default.any, props => {
    if (props.multiple && props.defaultValue !== undefined && !Array.isArray(props.defaultValue)) {
      return new Error(['MUI: The Autocomplete expects the `defaultValue` prop to be an array when `multiple={true}` or undefined.', `However, ${props.defaultValue} was provided.`].join('\n'));
    }
    return null;
  }),
  /**
   * If `true`, the input can't be cleared.
   * @default false
   */
  disableClearable: _propTypes.default.bool,
  /**
   * If `true`, the popup won't close when a value is selected.
   * @default false
   */
  disableCloseOnSelect: _propTypes.default.bool,
  /**
   * If `true`, the component is disabled.
   * @default false
   */
  disabled: _propTypes.default.bool,
  /**
   * If `true`, will allow focus on disabled items.
   * @default false
   */
  disabledItemsFocusable: _propTypes.default.bool,
  /**
   * If `true`, the list box in the popup will not wrap focus.
   * @default false
   */
  disableListWrap: _propTypes.default.bool,
  /**
   * If `true`, the `Popper` content will be under the DOM hierarchy of the parent component.
   * @default false
   */
  disablePortal: _propTypes.default.bool,
  /**
   * A function that determines the filtered options to be rendered on search.
   *
   * @default createFilterOptions()
   * @param {Value[]} options The options to render.
   * @param {object} state The state of the component.
   * @returns {Value[]}
   */
  filterOptions: _propTypes.default.func,
  /**
   * If `true`, hide the selected options from the list box.
   * @default false
   */
  filterSelectedOptions: _propTypes.default.bool,
  /**
   * Force the visibility display of the popup icon.
   * @default 'auto'
   */
  forcePopupIcon: _propTypes.default.oneOfType([_propTypes.default.oneOf(['auto']), _propTypes.default.bool]),
  /**
   * If `true`, the Autocomplete is free solo, meaning that the user input is not bound to provided options.
   * @default false
   */
  freeSolo: _propTypes.default.bool,
  /**
   * If `true`, the input will take up the full width of its container.
   * @default false
   */
  fullWidth: _propTypes.default.bool,
  /**
   * The label to display when the tags are truncated (`limitTags`).
   *
   * @param {number} more The number of truncated tags.
   * @returns {ReactNode}
   * @default (more) => `+${more}`
   */
  getLimitTagsText: _propTypes.default.func,
  /**
   * Used to determine the disabled state for a given option.
   *
   * @param {Value} option The option to test.
   * @returns {boolean}
   */
  getOptionDisabled: _propTypes.default.func,
  /**
   * Used to determine the key for a given option.
   * This can be useful when the labels of options are not unique (since labels are used as keys by default).
   *
   * @param {Value} option The option to get the key for.
   * @returns {string | number}
   */
  getOptionKey: _propTypes.default.func,
  /**
   * Used to determine the string value for a given option.
   * It's used to fill the input (and the list box options if `renderOption` is not provided).
   *
   * If used in free solo mode, it must accept both the type of the options and a string.
   *
   * @param {Value} option
   * @returns {string}
   * @default (option) => option.label ?? option
   */
  getOptionLabel: _propTypes.default.func,
  /**
   * If provided, the options will be grouped under the returned string.
   * The groupBy value is also used as the text for group headings when `renderGroup` is not provided.
   *
   * @param {Value} options The options to group.
   * @returns {string}
   */
  groupBy: _propTypes.default.func,
  /**
   * If `true`, the component handles the "Home" and "End" keys when the popup is open.
   * It should move focus to the first option and last option, respectively.
   * @default !props.freeSolo
   */
  handleHomeEndKeys: _propTypes.default.bool,
  /**
   * This prop is used to help implement the accessibility logic.
   * If you don't provide an id it will fall back to a randomly generated one.
   */
  id: _propTypes.default.string,
  /**
   * If `true`, the highlight can move to the input.
   * @default false
   */
  includeInputInList: _propTypes.default.bool,
  /**
   * The input value.
   */
  inputValue: _propTypes.default.string,
  /**
   * Used to determine if the option represents the given value.
   * Uses strict equality by default.
   * ⚠️ Both arguments need to be handled, an option can only match with one value.
   *
   * @param {Value} option The option to test.
   * @param {Value} value The value to test against.
   * @returns {boolean}
   */
  isOptionEqualToValue: _propTypes.default.func,
  /**
   * The maximum number of tags that will be visible when not focused.
   * Set `-1` to disable the limit.
   * @default -1
   */
  limitTags: _integerPropType.default,
  /**
   * The component used to render the listbox.
   * @default 'ul'
   */
  ListboxComponent: _propTypes.default.elementType,
  /**
   * Props applied to the Listbox element.
   */
  ListboxProps: _propTypes.default.object,
  /**
   * If `true`, the component is in a loading state.
   * This shows the `loadingText` in place of suggestions (only if there are no suggestions to show, for example `options` are empty).
   * @default false
   */
  loading: _propTypes.default.bool,
  /**
   * Text to display when in a loading state.
   *
   * For localization purposes, you can use the provided [translations](https://mui.com/material-ui/guides/localization/).
   * @default 'Loading…'
   */
  loadingText: _propTypes.default.node,
  /**
   * If `true`, `value` must be an array and the menu will support multiple selections.
   * @default false
   */
  multiple: _propTypes.default.bool,
  /**
   * Text to display when there are no options.
   *
   * For localization purposes, you can use the provided [translations](https://mui.com/material-ui/guides/localization/).
   * @default 'No options'
   */
  noOptionsText: _propTypes.default.node,
  /**
   * Callback fired when the value changes.
   *
   * @param {React.SyntheticEvent} event The event source of the callback.
   * @param {Value|Value[]} value The new value of the component.
   * @param {string} reason One of "createOption", "selectOption", "removeOption", "blur" or "clear".
   * @param {string} [details]
   */
  onChange: _propTypes.default.func,
  /**
   * Callback fired when the popup requests to be closed.
   * Use in controlled mode (see open).
   *
   * @param {React.SyntheticEvent} event The event source of the callback.
   * @param {string} reason Can be: `"toggleInput"`, `"escape"`, `"selectOption"`, `"removeOption"`, `"blur"`.
   */
  onClose: _propTypes.default.func,
  /**
   * Callback fired when the highlight option changes.
   *
   * @param {React.SyntheticEvent} event The event source of the callback.
   * @param {Value} option The highlighted option.
   * @param {string} reason Can be: `"keyboard"`, `"auto"`, `"mouse"`, `"touch"`.
   */
  onHighlightChange: _propTypes.default.func,
  /**
   * Callback fired when the input value changes.
   *
   * @param {React.SyntheticEvent} event The event source of the callback.
   * @param {string} value The new value of the text input.
   * @param {string} reason Can be: `"input"` (user input), `"reset"` (programmatic change), `"clear"`, `"blur"`, `"selectOption"`, `"removeOption"`
   */
  onInputChange: _propTypes.default.func,
  /**
   * @ignore
   */
  onKeyDown: _propTypes.default.func,
  /**
   * Callback fired when the popup requests to be opened.
   * Use in controlled mode (see open).
   *
   * @param {React.SyntheticEvent} event The event source of the callback.
   */
  onOpen: _propTypes.default.func,
  /**
   * If `true`, the component is shown.
   */
  open: _propTypes.default.bool,
  /**
   * If `true`, the popup will open on input focus.
   * @default false
   */
  openOnFocus: _propTypes.default.bool,
  /**
   * Override the default text for the *open popup* icon button.
   *
   * For localization purposes, you can use the provided [translations](https://mui.com/material-ui/guides/localization/).
   * @default 'Open'
   */
  openText: _propTypes.default.string,
  /**
   * A list of options that will be shown in the Autocomplete.
   */
  options: _propTypes.default.array.isRequired,
  /**
   * The component used to render the body of the popup.
   * @default Paper
   */
  PaperComponent: _propTypes.default.elementType,
  /**
   * The component used to position the popup.
   * @default Popper
   */
  PopperComponent: _propTypes.default.elementType,
  /**
   * The icon to display in place of the default popup icon.
   * @default <ArrowDropDownIcon />
   */
  popupIcon: _propTypes.default.node,
  /**
   * If `true`, the component becomes readonly. It is also supported for multiple tags where the tag cannot be deleted.
   * @default false
   */
  readOnly: _propTypes.default.bool,
  /**
   * Render the group.
   *
   * @param {AutocompleteRenderGroupParams} params The group to render.
   * @returns {ReactNode}
   */
  renderGroup: _propTypes.default.func,
  /**
   * Render the input.
   *
   * @param {object} params
   * @returns {ReactNode}
   */
  renderInput: _propTypes.default.func.isRequired,
  /**
   * Render the option, use `getOptionLabel` by default.
   *
   * @param {object} props The props to apply on the li element.
   * @param {Value} option The option to render.
   * @param {object} state The state of each option.
   * @param {object} ownerState The state of the Autocomplete component.
   * @returns {ReactNode}
   */
  renderOption: _propTypes.default.func,
  /**
   * Render the selected value.
   *
   * @param {Value[]} value The `value` provided to the component.
   * @param {function} getTagProps A tag props getter.
   * @param {object} ownerState The state of the Autocomplete component.
   * @returns {ReactNode}
   */
  renderTags: _propTypes.default.func,
  /**
   * If `true`, the input's text is selected on focus.
   * It helps the user clear the selected value.
   * @default !props.freeSolo
   */
  selectOnFocus: _propTypes.default.bool,
  /**
   * The size of the component.
   * @default 'medium'
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['small', 'medium']), _propTypes.default.string]),
  /**
   * The props used for each slot inside.
   * @default {}
   */
  slotProps: _propTypes.default /* @typescript-to-proptypes-ignore */.shape({
    chip: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    clearIndicator: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    listbox: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    paper: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    popper: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]),
    popupIndicator: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object])
  }),
  /**
   * The components used for each slot inside.
   * @default {}
   */
  slots: _propTypes.default.shape({
    listbox: _propTypes.default.elementType,
    paper: _propTypes.default.elementType,
    popper: _propTypes.default.elementType
  }),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The value of the autocomplete.
   *
   * The value must have reference equality with the option in order to be selected.
   * You can customize the equality behavior with the `isOptionEqualToValue` prop.
   */
  value: (0, _chainPropTypes.default)(_propTypes.default.any, props => {
    if (props.multiple && props.value !== undefined && !Array.isArray(props.value)) {
      return new Error(['MUI: The Autocomplete expects the `value` prop to be an array when `multiple={true}` or undefined.', `However, ${props.value} was provided.`].join('\n'));
    }
    return null;
  })
} : void 0;
var _default = exports.default = Autocomplete;