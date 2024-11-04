"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useUtils = exports.useNow = exports.useLocalizationContext = exports.useDefaultDates = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var React = _interopRequireWildcard(require("react"));
var _LocalizationProvider = require("../../LocalizationProvider/LocalizationProvider");
var _enUS = require("../../locales/enUS");
const useLocalizationContext = () => {
  const localization = React.useContext(_LocalizationProvider.MuiPickersAdapterContext);
  if (localization === null) {
    throw new Error(['MUI X: Can not find the date and time pickers localization context.', 'It looks like you forgot to wrap your component in LocalizationProvider.', 'This can also happen if you are bundling multiple versions of the `@mui/x-date-pickers` package'].join('\n'));
  }
  if (localization.utils === null) {
    throw new Error(['MUI X: Can not find the date and time pickers adapter from its localization context.', 'It looks like you forgot to pass a `dateAdapter` to your LocalizationProvider.'].join('\n'));
  }
  const localeText = React.useMemo(() => (0, _extends2.default)({}, _enUS.DEFAULT_LOCALE, localization.localeText), [localization.localeText]);
  return React.useMemo(() => (0, _extends2.default)({}, localization, {
    localeText
  }), [localization, localeText]);
};
exports.useLocalizationContext = useLocalizationContext;
const useUtils = () => useLocalizationContext().utils;
exports.useUtils = useUtils;
const useDefaultDates = () => useLocalizationContext().defaultDates;
exports.useDefaultDates = useDefaultDates;
const useNow = timezone => {
  const utils = useUtils();
  const now = React.useRef();
  if (now.current === undefined) {
    now.current = utils.date(undefined, timezone);
  }
  return now.current;
};
exports.useNow = useNow;