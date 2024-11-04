'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import composeClasses from '@mui/utils/composeClasses';
import { styled } from "../zero-styled/index.js";
import memoTheme from "../utils/memoTheme.js";
import { useDefaultProps } from "../DefaultPropsProvider/index.js";
import Typography from "../Typography/index.js";
import { getAlertTitleUtilityClass } from "./alertTitleClasses.js";
import { jsx as _jsx } from "react/jsx-runtime";
const useUtilityClasses = ownerState => {
  const {
    classes
  } = ownerState;
  const slots = {
    root: ['root']
  };
  return composeClasses(slots, getAlertTitleUtilityClass, classes);
};
const AlertTitleRoot = styled(Typography, {
  name: 'MuiAlertTitle',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})(memoTheme(({
  theme
}) => {
  return {
    fontWeight: theme.typography.fontWeightMedium,
    marginTop: -2
  };
}));
const AlertTitle = /*#__PURE__*/React.forwardRef(function AlertTitle(inProps, ref) {
  const props = useDefaultProps({
    props: inProps,
    name: 'MuiAlertTitle'
  });
  const {
    className,
    ...other
  } = props;
  const ownerState = props;
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/_jsx(AlertTitleRoot, {
    gutterBottom: true,
    component: "div",
    ownerState: ownerState,
    ref: ref,
    className: clsx(classes.root, className),
    ...other
  });
});
process.env.NODE_ENV !== "production" ? AlertTitle.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
   */
  children: PropTypes.node,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: PropTypes.object,
  /**
   * @ignore
   */
  className: PropTypes.string,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.func, PropTypes.object, PropTypes.bool])), PropTypes.func, PropTypes.object])
} : void 0;
export default AlertTitle;