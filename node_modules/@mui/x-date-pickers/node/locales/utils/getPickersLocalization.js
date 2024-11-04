"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.getPickersLocalization = exports.buildGetOpenDialogAriaText = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
const getPickersLocalization = pickersTranslations => {
  return {
    components: {
      MuiLocalizationProvider: {
        defaultProps: {
          localeText: (0, _extends2.default)({}, pickersTranslations)
        }
      }
    }
  };
};
exports.getPickersLocalization = getPickersLocalization;
const buildGetOpenDialogAriaText = params => {
  const {
    utils,
    formatKey,
    contextTranslation,
    propsTranslation
  } = params;
  return value => {
    const formattedValue = value !== null && utils.isValid(value) ? utils.format(value, formatKey) : null;
    const translation = propsTranslation ?? contextTranslation;
    return translation(value, utils, formattedValue);
  };
};
exports.buildGetOpenDialogAriaText = buildGetOpenDialogAriaText;