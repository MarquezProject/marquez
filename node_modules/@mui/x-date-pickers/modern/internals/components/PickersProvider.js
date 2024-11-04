import * as React from 'react';
import { LocalizationProvider } from "../../LocalizationProvider/index.js";
import { jsx as _jsx } from "react/jsx-runtime";
export const PickersContext = /*#__PURE__*/React.createContext(null);

/**
 * Provides the context for the various parts of a picker component:
 * - contextValue: the context for the picker sub-components.
 * - localizationProvider: the translations passed through the props and through a parent LocalizationProvider.
 *
 * @ignore - do not document.
 */
export function PickersProvider(props) {
  const {
    contextValue,
    localeText,
    children
  } = props;
  return /*#__PURE__*/_jsx(PickersContext.Provider, {
    value: contextValue,
    children: /*#__PURE__*/_jsx(LocalizationProvider, {
      localeText: localeText,
      children: children
    })
  });
}