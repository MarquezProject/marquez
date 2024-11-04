'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import resolveProps from '@mui/utils/resolveProps';
import { jsx as _jsx } from "react/jsx-runtime";
const PropsContext = /*#__PURE__*/React.createContext(undefined);
function DefaultPropsProvider({
  value,
  children
}) {
  return /*#__PURE__*/_jsx(PropsContext.Provider, {
    value: value,
    children: children
  });
}
process.env.NODE_ENV !== "production" ? DefaultPropsProvider.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │ To update them, edit the TypeScript types and run `pnpm proptypes`. │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * @ignore
   */
  children: PropTypes.node,
  /**
   * @ignore
   */
  value: PropTypes.object
} : void 0;
function getThemeProps(params) {
  const {
    theme,
    name,
    props
  } = params;
  if (!theme || !theme.components || !theme.components[name]) {
    return props;
  }
  const config = theme.components[name];
  if (config.defaultProps) {
    // compatible with v5 signature
    return resolveProps(config.defaultProps, props);
  }
  if (!config.styleOverrides && !config.variants) {
    // v6 signature, no property 'defaultProps'
    return resolveProps(config, props);
  }
  return props;
}
export function useDefaultProps({
  props,
  name
}) {
  const ctx = React.useContext(PropsContext);
  return getThemeProps({
    props,
    name,
    theme: {
      components: ctx
    }
  });
}
export default DefaultPropsProvider;