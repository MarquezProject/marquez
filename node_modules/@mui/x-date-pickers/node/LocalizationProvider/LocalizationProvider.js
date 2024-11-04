"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.MuiPickersAdapterContext = exports.LocalizationProvider = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var _objectWithoutPropertiesLoose2 = _interopRequireDefault(require("@babel/runtime/helpers/objectWithoutPropertiesLoose"));
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _styles = require("@mui/material/styles");
var _jsxRuntime = require("react/jsx-runtime");
const _excluded = ["localeText"];
const MuiPickersAdapterContext = exports.MuiPickersAdapterContext = /*#__PURE__*/React.createContext(null);
if (process.env.NODE_ENV !== 'production') {
  MuiPickersAdapterContext.displayName = 'MuiPickersAdapterContext';
}
/**
 * Demos:
 *
 * - [Date format and localization](https://mui.com/x/react-date-pickers/adapters-locale/)
 * - [Calendar systems](https://mui.com/x/react-date-pickers/calendar-systems/)
 * - [Translated components](https://mui.com/x/react-date-pickers/localization/)
 * - [UTC and timezones](https://mui.com/x/react-date-pickers/timezone/)
 *
 * API:
 *
 * - [LocalizationProvider API](https://mui.com/x/api/date-pickers/localization-provider/)
 */
const LocalizationProvider = exports.LocalizationProvider = function LocalizationProvider(inProps) {
  const {
      localeText: inLocaleText
    } = inProps,
    otherInProps = (0, _objectWithoutPropertiesLoose2.default)(inProps, _excluded);
  const {
    utils: parentUtils,
    localeText: parentLocaleText
  } = React.useContext(MuiPickersAdapterContext) ?? {
    utils: undefined,
    localeText: undefined
  };
  const props = (0, _styles.useThemeProps)({
    // We don't want to pass the `localeText` prop to the theme, that way it will always return the theme value,
    // We will then merge this theme value with our value manually
    props: otherInProps,
    name: 'MuiLocalizationProvider'
  });
  const {
    children,
    dateAdapter: DateAdapter,
    dateFormats,
    dateLibInstance,
    adapterLocale,
    localeText: themeLocaleText
  } = props;
  const localeText = React.useMemo(() => (0, _extends2.default)({}, themeLocaleText, parentLocaleText, inLocaleText), [themeLocaleText, parentLocaleText, inLocaleText]);
  const utils = React.useMemo(() => {
    if (!DateAdapter) {
      if (parentUtils) {
        return parentUtils;
      }
      return null;
    }
    const adapter = new DateAdapter({
      locale: adapterLocale,
      formats: dateFormats,
      instance: dateLibInstance
    });
    if (!adapter.isMUIAdapter) {
      throw new Error(['MUI X: The date adapter should be imported from `@mui/x-date-pickers` or `@mui/x-date-pickers-pro`, not from `@date-io`', "For example, `import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'` instead of `import AdapterDayjs from '@date-io/dayjs'`", 'More information on the installation documentation: https://mui.com/x/react-date-pickers/getting-started/#installation'].join(`\n`));
    }
    return adapter;
  }, [DateAdapter, adapterLocale, dateFormats, dateLibInstance, parentUtils]);
  const defaultDates = React.useMemo(() => {
    if (!utils) {
      return null;
    }
    return {
      minDate: utils.date('1900-01-01T00:00:00.000'),
      maxDate: utils.date('2099-12-31T00:00:00.000')
    };
  }, [utils]);
  const contextValue = React.useMemo(() => {
    return {
      utils,
      defaultDates,
      localeText
    };
  }, [defaultDates, utils, localeText]);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(MuiPickersAdapterContext.Provider, {
    value: contextValue,
    children: children
  });
};
process.env.NODE_ENV !== "production" ? LocalizationProvider.propTypes = {
  // ----------------------------- Warning --------------------------------
  // | These PropTypes are generated from the TypeScript type definitions |
  // | To update them edit the TypeScript types and run "pnpm proptypes"  |
  // ----------------------------------------------------------------------
  /**
   * Locale for the date library you are using
   */
  adapterLocale: _propTypes.default.any,
  children: _propTypes.default.node,
  /**
   * Date library adapter class function.
   * @see See the localization provider {@link https://mui.com/x/react-date-pickers/getting-started/#setup-your-date-library-adapter date adapter setup section} for more details.
   */
  dateAdapter: _propTypes.default.func,
  /**
   * Formats that are used for any child pickers
   */
  dateFormats: _propTypes.default.shape({
    dayOfMonth: _propTypes.default.string,
    dayOfMonthFull: _propTypes.default.string,
    fullDate: _propTypes.default.string,
    fullTime: _propTypes.default.string,
    fullTime12h: _propTypes.default.string,
    fullTime24h: _propTypes.default.string,
    hours12h: _propTypes.default.string,
    hours24h: _propTypes.default.string,
    keyboardDate: _propTypes.default.string,
    keyboardDateTime: _propTypes.default.string,
    keyboardDateTime12h: _propTypes.default.string,
    keyboardDateTime24h: _propTypes.default.string,
    meridiem: _propTypes.default.string,
    minutes: _propTypes.default.string,
    month: _propTypes.default.string,
    monthShort: _propTypes.default.string,
    normalDate: _propTypes.default.string,
    normalDateWithWeekday: _propTypes.default.string,
    seconds: _propTypes.default.string,
    shortDate: _propTypes.default.string,
    weekday: _propTypes.default.string,
    weekdayShort: _propTypes.default.string,
    year: _propTypes.default.string
  }),
  /**
   * Date library instance you are using, if it has some global overrides
   * ```jsx
   * dateLibInstance={momentTimeZone}
   * ```
   */
  dateLibInstance: _propTypes.default.any,
  /**
   * Locale for components texts
   */
  localeText: _propTypes.default.object
} : void 0;