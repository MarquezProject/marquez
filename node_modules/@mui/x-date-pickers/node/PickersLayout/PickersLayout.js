"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.PickersLayoutRoot = exports.PickersLayoutContentWrapper = exports.PickersLayout = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _styles = require("@mui/material/styles");
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _pickersLayoutClasses = require("./pickersLayoutClasses");
var _usePickerLayout = _interopRequireDefault(require("./usePickerLayout"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    isLandscape,
    classes
  } = ownerState;
  const slots = {
    root: ['root', isLandscape && 'landscape'],
    contentWrapper: ['contentWrapper']
  };
  return (0, _composeClasses.default)(slots, _pickersLayoutClasses.getPickersLayoutUtilityClass, classes);
};
const PickersLayoutRoot = exports.PickersLayoutRoot = (0, _styles.styled)('div', {
  name: 'MuiPickersLayout',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})({
  display: 'grid',
  gridAutoColumns: 'max-content auto max-content',
  gridAutoRows: 'max-content auto max-content',
  [`& .${_pickersLayoutClasses.pickersLayoutClasses.actionBar}`]: {
    gridColumn: '1 / 4',
    gridRow: 3
  },
  variants: [{
    props: {
      isLandscape: true
    },
    style: {
      [`& .${_pickersLayoutClasses.pickersLayoutClasses.toolbar}`]: {
        gridColumn: 1,
        gridRow: '2 / 3'
      },
      [`.${_pickersLayoutClasses.pickersLayoutClasses.shortcuts}`]: {
        gridColumn: '2 / 4',
        gridRow: 1
      }
    }
  }, {
    props: {
      isLandscape: true,
      isRtl: true
    },
    style: {
      [`& .${_pickersLayoutClasses.pickersLayoutClasses.toolbar}`]: {
        gridColumn: 3
      }
    }
  }, {
    props: {
      isLandscape: false
    },
    style: {
      [`& .${_pickersLayoutClasses.pickersLayoutClasses.toolbar}`]: {
        gridColumn: '2 / 4',
        gridRow: 1
      },
      [`& .${_pickersLayoutClasses.pickersLayoutClasses.shortcuts}`]: {
        gridColumn: 1,
        gridRow: '2 / 3'
      }
    }
  }, {
    props: {
      isLandscape: false,
      isRtl: true
    },
    style: {
      [`& .${_pickersLayoutClasses.pickersLayoutClasses.shortcuts}`]: {
        gridColumn: 3
      }
    }
  }]
});
const PickersLayoutContentWrapper = exports.PickersLayoutContentWrapper = (0, _styles.styled)('div', {
  name: 'MuiPickersLayout',
  slot: 'ContentWrapper',
  overridesResolver: (props, styles) => styles.contentWrapper
})({
  gridColumn: 2,
  gridRow: 2,
  display: 'flex',
  flexDirection: 'column'
});
/**
 * Demos:
 *
 * - [Custom layout](https://mui.com/x/react-date-pickers/custom-layout/)
 *
 * API:
 *
 * - [PickersLayout API](https://mui.com/x/api/date-pickers/pickers-layout/)
 */
const PickersLayout = exports.PickersLayout = /*#__PURE__*/React.forwardRef(function PickersLayout(inProps, ref) {
  const props = (0, _styles.useThemeProps)({
    props: inProps,
    name: 'MuiPickersLayout'
  });
  const {
    toolbar,
    content,
    tabs,
    actionBar,
    shortcuts
  } = (0, _usePickerLayout.default)(props);
  const {
    sx,
    className,
    isLandscape,
    wrapperVariant
  } = props;
  const classes = useUtilityClasses(props);
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(PickersLayoutRoot, {
    ref: ref,
    sx: sx,
    className: (0, _clsx.default)(classes.root, className),
    ownerState: props,
    children: [isLandscape ? shortcuts : toolbar, isLandscape ? toolbar : shortcuts, /*#__PURE__*/(0, _jsxRuntime.jsx)(PickersLayoutContentWrapper, {
      className: classes.contentWrapper,
      children: wrapperVariant === 'desktop' ? /*#__PURE__*/(0, _jsxRuntime.jsxs)(React.Fragment, {
        children: [content, tabs]
      }) : /*#__PURE__*/(0, _jsxRuntime.jsxs)(React.Fragment, {
        children: [tabs, content]
      })
    }), actionBar]
  });
});
process.env.NODE_ENV !== "production" ? PickersLayout.propTypes = {
  // ----------------------------- Warning --------------------------------
  // | These PropTypes are generated from the TypeScript type definitions |
  // | To update them edit the TypeScript types and run "pnpm proptypes"  |
  // ----------------------------------------------------------------------
  children: _propTypes.default.node,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  className: _propTypes.default.string,
  disabled: _propTypes.default.bool,
  isLandscape: _propTypes.default.bool.isRequired,
  /**
   * `true` if the application is in right-to-left direction.
   */
  isRtl: _propTypes.default.bool.isRequired,
  isValid: _propTypes.default.func.isRequired,
  onAccept: _propTypes.default.func.isRequired,
  onCancel: _propTypes.default.func.isRequired,
  onChange: _propTypes.default.func.isRequired,
  onClear: _propTypes.default.func.isRequired,
  onClose: _propTypes.default.func.isRequired,
  onDismiss: _propTypes.default.func.isRequired,
  onOpen: _propTypes.default.func.isRequired,
  onSelectShortcut: _propTypes.default.func.isRequired,
  onSetToday: _propTypes.default.func.isRequired,
  onViewChange: _propTypes.default.func.isRequired,
  /**
   * Force rendering in particular orientation.
   */
  orientation: _propTypes.default.oneOf(['landscape', 'portrait']),
  readOnly: _propTypes.default.bool,
  /**
   * The props used for each component slot.
   * @default {}
   */
  slotProps: _propTypes.default.object,
  /**
   * Overridable component slots.
   * @default {}
   */
  slots: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  value: _propTypes.default.any,
  view: _propTypes.default.oneOf(['day', 'hours', 'meridiem', 'minutes', 'month', 'seconds', 'year']),
  views: _propTypes.default.arrayOf(_propTypes.default.oneOf(['day', 'hours', 'meridiem', 'minutes', 'month', 'seconds', 'year']).isRequired).isRequired,
  wrapperVariant: _propTypes.default.oneOf(['desktop', 'mobile'])
} : void 0;