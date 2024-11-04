"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _privateTheming = require("@mui/private-theming");
var _exactProp = _interopRequireDefault(require("@mui/utils/exactProp"));
var _styledEngine = require("@mui/styled-engine");
var _useThemeWithoutDefault = _interopRequireDefault(require("../useThemeWithoutDefault"));
var _RtlProvider = _interopRequireDefault(require("../RtlProvider"));
var _DefaultPropsProvider = _interopRequireDefault(require("../DefaultPropsProvider"));
var _jsxRuntime = require("react/jsx-runtime");
const EMPTY_THEME = {};
function useThemeScoping(themeId, upperTheme, localTheme, isPrivate = false) {
  return React.useMemo(() => {
    const resolvedTheme = themeId ? upperTheme[themeId] || upperTheme : upperTheme;
    if (typeof localTheme === 'function') {
      const mergedTheme = localTheme(resolvedTheme);
      const result = themeId ? {
        ...upperTheme,
        [themeId]: mergedTheme
      } : mergedTheme;
      // must return a function for the private theme to NOT merge with the upper theme.
      // see the test case "use provided theme from a callback" in ThemeProvider.test.js
      if (isPrivate) {
        return () => result;
      }
      return result;
    }
    return themeId ? {
      ...upperTheme,
      [themeId]: localTheme
    } : {
      ...upperTheme,
      ...localTheme
    };
  }, [themeId, upperTheme, localTheme, isPrivate]);
}

/**
 * This component makes the `theme` available down the React tree.
 * It should preferably be used at **the root of your component tree**.
 *
 * <ThemeProvider theme={theme}> // existing use case
 * <ThemeProvider theme={{ id: theme }}> // theme scoping
 */
function ThemeProvider(props) {
  const {
    children,
    theme: localTheme,
    themeId
  } = props;
  const upperTheme = (0, _useThemeWithoutDefault.default)(EMPTY_THEME);
  const upperPrivateTheme = (0, _privateTheming.useTheme)() || EMPTY_THEME;
  if (process.env.NODE_ENV !== 'production') {
    if (upperTheme === null && typeof localTheme === 'function' || themeId && upperTheme && !upperTheme[themeId] && typeof localTheme === 'function') {
      console.error(['MUI: You are providing a theme function prop to the ThemeProvider component:', '<ThemeProvider theme={outerTheme => outerTheme} />', '', 'However, no outer theme is present.', 'Make sure a theme is already injected higher in the React tree ' + 'or provide a theme object.'].join('\n'));
    }
  }
  const engineTheme = useThemeScoping(themeId, upperTheme, localTheme);
  const privateTheme = useThemeScoping(themeId, upperPrivateTheme, localTheme, true);
  const rtlValue = engineTheme.direction === 'rtl';
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_privateTheming.ThemeProvider, {
    theme: privateTheme,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(_styledEngine.ThemeContext.Provider, {
      value: engineTheme,
      children: /*#__PURE__*/(0, _jsxRuntime.jsx)(_RtlProvider.default, {
        value: rtlValue,
        children: /*#__PURE__*/(0, _jsxRuntime.jsx)(_DefaultPropsProvider.default, {
          value: engineTheme?.components,
          children: children
        })
      })
    })
  });
}
process.env.NODE_ENV !== "production" ? ThemeProvider.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Your component tree.
   */
  children: _propTypes.default.node,
  /**
   * A theme object. You can provide a function to extend the outer theme.
   */
  theme: _propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object]).isRequired,
  /**
   * The design system's unique id for getting the corresponded theme when there are multiple design systems.
   */
  themeId: _propTypes.default.string
} : void 0;
if (process.env.NODE_ENV !== 'production') {
  process.env.NODE_ENV !== "production" ? ThemeProvider.propTypes = (0, _exactProp.default)(ThemeProvider.propTypes) : void 0;
}
var _default = exports.default = ThemeProvider;