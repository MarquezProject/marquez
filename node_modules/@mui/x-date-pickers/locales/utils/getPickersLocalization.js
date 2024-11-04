import _extends from "@babel/runtime/helpers/esm/extends";
export const getPickersLocalization = pickersTranslations => {
  return {
    components: {
      MuiLocalizationProvider: {
        defaultProps: {
          localeText: _extends({}, pickersTranslations)
        }
      }
    }
  };
};
export const buildGetOpenDialogAriaText = params => {
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