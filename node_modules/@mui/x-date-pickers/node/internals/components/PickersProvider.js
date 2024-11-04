"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.PickersContext = void 0;
exports.PickersProvider = PickersProvider;
var React = _interopRequireWildcard(require("react"));
var _LocalizationProvider = require("../../LocalizationProvider");
var _jsxRuntime = require("react/jsx-runtime");
const PickersContext = exports.PickersContext = /*#__PURE__*/React.createContext(null);

/**
 * Provides the context for the various parts of a picker component:
 * - contextValue: the context for the picker sub-components.
 * - localizationProvider: the translations passed through the props and through a parent LocalizationProvider.
 *
 * @ignore - do not document.
 */
function PickersProvider(props) {
  const {
    contextValue,
    localeText,
    children
  } = props;
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(PickersContext.Provider, {
    value: contextValue,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(_LocalizationProvider.LocalizationProvider, {
      localeText: localeText,
      children: children
    })
  });
}