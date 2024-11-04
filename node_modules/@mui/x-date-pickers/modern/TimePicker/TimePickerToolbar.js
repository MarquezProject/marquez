'use client';

import _extends from "@babel/runtime/helpers/esm/extends";
import _objectWithoutPropertiesLoose from "@babel/runtime/helpers/esm/objectWithoutPropertiesLoose";
const _excluded = ["ampm", "ampmInClock", "value", "isLandscape", "onChange", "view", "onViewChange", "views", "disabled", "readOnly", "className"];
import * as React from 'react';
import clsx from 'clsx';
import PropTypes from 'prop-types';
import { useRtl } from '@mui/system/RtlProvider';
import { styled, useThemeProps } from '@mui/material/styles';
import composeClasses from '@mui/utils/composeClasses';
import { PickersToolbarText } from "../internals/components/PickersToolbarText.js";
import { PickersToolbarButton } from "../internals/components/PickersToolbarButton.js";
import { PickersToolbar } from "../internals/components/PickersToolbar.js";
import { arrayIncludes } from "../internals/utils/utils.js";
import { usePickersTranslations } from "../hooks/usePickersTranslations.js";
import { useUtils } from "../internals/hooks/useUtils.js";
import { useMeridiemMode } from "../internals/hooks/date-helpers-hooks.js";
import { getTimePickerToolbarUtilityClass, timePickerToolbarClasses } from "./timePickerToolbarClasses.js";
import { formatMeridiem } from "../internals/utils/date-utils.js";
import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
const useUtilityClasses = ownerState => {
  const {
    isLandscape,
    classes,
    isRtl
  } = ownerState;
  const slots = {
    root: ['root'],
    separator: ['separator'],
    hourMinuteLabel: ['hourMinuteLabel', isLandscape && 'hourMinuteLabelLandscape', isRtl && 'hourMinuteLabelReverse'],
    ampmSelection: ['ampmSelection', isLandscape && 'ampmLandscape'],
    ampmLabel: ['ampmLabel']
  };
  return composeClasses(slots, getTimePickerToolbarUtilityClass, classes);
};
const TimePickerToolbarRoot = styled(PickersToolbar, {
  name: 'MuiTimePickerToolbar',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})({});
const TimePickerToolbarSeparator = styled(PickersToolbarText, {
  name: 'MuiTimePickerToolbar',
  slot: 'Separator',
  overridesResolver: (props, styles) => styles.separator
})({
  outline: 0,
  margin: '0 4px 0 2px',
  cursor: 'default'
});
const TimePickerToolbarHourMinuteLabel = styled('div', {
  name: 'MuiTimePickerToolbar',
  slot: 'HourMinuteLabel',
  overridesResolver: (props, styles) => [{
    [`&.${timePickerToolbarClasses.hourMinuteLabelLandscape}`]: styles.hourMinuteLabelLandscape,
    [`&.${timePickerToolbarClasses.hourMinuteLabelReverse}`]: styles.hourMinuteLabelReverse
  }, styles.hourMinuteLabel]
})({
  display: 'flex',
  justifyContent: 'flex-end',
  alignItems: 'flex-end',
  variants: [{
    props: {
      isRtl: true
    },
    style: {
      flexDirection: 'row-reverse'
    }
  }, {
    props: {
      isLandscape: true
    },
    style: {
      marginTop: 'auto'
    }
  }]
});
const TimePickerToolbarAmPmSelection = styled('div', {
  name: 'MuiTimePickerToolbar',
  slot: 'AmPmSelection',
  overridesResolver: (props, styles) => [{
    [`.${timePickerToolbarClasses.ampmLabel}`]: styles.ampmLabel
  }, {
    [`&.${timePickerToolbarClasses.ampmLandscape}`]: styles.ampmLandscape
  }, styles.ampmSelection]
})({
  display: 'flex',
  flexDirection: 'column',
  marginRight: 'auto',
  marginLeft: 12,
  [`& .${timePickerToolbarClasses.ampmLabel}`]: {
    fontSize: 17
  },
  variants: [{
    props: {
      isLandscape: true
    },
    style: {
      margin: '4px 0 auto',
      flexDirection: 'row',
      justifyContent: 'space-around',
      flexBasis: '100%'
    }
  }]
});

/**
 * Demos:
 *
 * - [TimePicker](https://mui.com/x/react-date-pickers/time-picker/)
 * - [Custom components](https://mui.com/x/react-date-pickers/custom-components/)
 *
 * API:
 *
 * - [TimePickerToolbar API](https://mui.com/x/api/date-pickers/time-picker-toolbar/)
 */
function TimePickerToolbar(inProps) {
  const props = useThemeProps({
    props: inProps,
    name: 'MuiTimePickerToolbar'
  });
  const {
      ampm,
      ampmInClock,
      value,
      isLandscape,
      onChange,
      view,
      onViewChange,
      views,
      disabled,
      readOnly,
      className
    } = props,
    other = _objectWithoutPropertiesLoose(props, _excluded);
  const utils = useUtils();
  const translations = usePickersTranslations();
  const isRtl = useRtl();
  const showAmPmControl = Boolean(ampm && !ampmInClock && views.includes('hours'));
  const {
    meridiemMode,
    handleMeridiemChange
  } = useMeridiemMode(value, ampm, onChange);
  const formatHours = time => ampm ? utils.format(time, 'hours12h') : utils.format(time, 'hours24h');
  const ownerState = _extends({}, props, {
    isRtl
  });
  const classes = useUtilityClasses(ownerState);
  const separator = /*#__PURE__*/_jsx(TimePickerToolbarSeparator, {
    tabIndex: -1,
    value: ":",
    variant: "h3",
    selected: false,
    className: classes.separator
  });
  return /*#__PURE__*/_jsxs(TimePickerToolbarRoot, _extends({
    landscapeDirection: "row",
    toolbarTitle: translations.timePickerToolbarTitle,
    isLandscape: isLandscape,
    ownerState: ownerState,
    className: clsx(classes.root, className)
  }, other, {
    children: [/*#__PURE__*/_jsxs(TimePickerToolbarHourMinuteLabel, {
      className: classes.hourMinuteLabel,
      ownerState: ownerState,
      children: [arrayIncludes(views, 'hours') && /*#__PURE__*/_jsx(PickersToolbarButton, {
        tabIndex: -1,
        variant: "h3",
        onClick: () => onViewChange('hours'),
        selected: view === 'hours',
        value: value ? formatHours(value) : '--'
      }), arrayIncludes(views, ['hours', 'minutes']) && separator, arrayIncludes(views, 'minutes') && /*#__PURE__*/_jsx(PickersToolbarButton, {
        tabIndex: -1,
        variant: "h3",
        onClick: () => onViewChange('minutes'),
        selected: view === 'minutes',
        value: value ? utils.format(value, 'minutes') : '--'
      }), arrayIncludes(views, ['minutes', 'seconds']) && separator, arrayIncludes(views, 'seconds') && /*#__PURE__*/_jsx(PickersToolbarButton, {
        variant: "h3",
        onClick: () => onViewChange('seconds'),
        selected: view === 'seconds',
        value: value ? utils.format(value, 'seconds') : '--'
      })]
    }), showAmPmControl && /*#__PURE__*/_jsxs(TimePickerToolbarAmPmSelection, {
      className: classes.ampmSelection,
      ownerState: ownerState,
      children: [/*#__PURE__*/_jsx(PickersToolbarButton, {
        disableRipple: true,
        variant: "subtitle2",
        selected: meridiemMode === 'am',
        typographyClassName: classes.ampmLabel,
        value: formatMeridiem(utils, 'am'),
        onClick: readOnly ? undefined : () => handleMeridiemChange('am'),
        disabled: disabled
      }), /*#__PURE__*/_jsx(PickersToolbarButton, {
        disableRipple: true,
        variant: "subtitle2",
        selected: meridiemMode === 'pm',
        typographyClassName: classes.ampmLabel,
        value: formatMeridiem(utils, 'pm'),
        onClick: readOnly ? undefined : () => handleMeridiemChange('pm'),
        disabled: disabled
      })]
    })]
  }));
}
process.env.NODE_ENV !== "production" ? TimePickerToolbar.propTypes = {
  // ----------------------------- Warning --------------------------------
  // | These PropTypes are generated from the TypeScript type definitions |
  // | To update them edit the TypeScript types and run "pnpm proptypes"  |
  // ----------------------------------------------------------------------
  ampm: PropTypes.bool,
  ampmInClock: PropTypes.bool,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: PropTypes.object,
  className: PropTypes.string,
  disabled: PropTypes.bool,
  /**
   * If `true`, show the toolbar even in desktop mode.
   * @default `true` for Desktop, `false` for Mobile.
   */
  hidden: PropTypes.bool,
  isLandscape: PropTypes.bool.isRequired,
  onChange: PropTypes.func.isRequired,
  /**
   * Callback called when a toolbar is clicked
   * @template TView
   * @param {TView} view The view to open
   */
  onViewChange: PropTypes.func.isRequired,
  readOnly: PropTypes.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.func, PropTypes.object, PropTypes.bool])), PropTypes.func, PropTypes.object]),
  titleId: PropTypes.string,
  /**
   * Toolbar date format.
   */
  toolbarFormat: PropTypes.string,
  /**
   * Toolbar value placeholder—it is displayed when the value is empty.
   * @default "––"
   */
  toolbarPlaceholder: PropTypes.node,
  value: PropTypes.object,
  /**
   * Currently visible picker view.
   */
  view: PropTypes.oneOf(['hours', 'meridiem', 'minutes', 'seconds']).isRequired,
  /**
   * Available views.
   */
  views: PropTypes.arrayOf(PropTypes.oneOf(['hours', 'meridiem', 'minutes', 'seconds']).isRequired).isRequired
} : void 0;
export { TimePickerToolbar };