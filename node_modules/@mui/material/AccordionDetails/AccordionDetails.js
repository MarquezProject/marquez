'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import composeClasses from '@mui/utils/composeClasses';
import { styled } from "../zero-styled/index.js";
import memoTheme from "../utils/memoTheme.js";
import { useDefaultProps } from "../DefaultPropsProvider/index.js";
import { getAccordionDetailsUtilityClass } from "./accordionDetailsClasses.js";
import { jsx as _jsx } from "react/jsx-runtime";
const useUtilityClasses = ownerState => {
  const {
    classes
  } = ownerState;
  const slots = {
    root: ['root']
  };
  return composeClasses(slots, getAccordionDetailsUtilityClass, classes);
};
const AccordionDetailsRoot = styled('div', {
  name: 'MuiAccordionDetails',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
})(memoTheme(({
  theme
}) => ({
  padding: theme.spacing(1, 2, 2)
})));
const AccordionDetails = /*#__PURE__*/React.forwardRef(function AccordionDetails(inProps, ref) {
  const props = useDefaultProps({
    props: inProps,
    name: 'MuiAccordionDetails'
  });
  const {
    className,
    ...other
  } = props;
  const ownerState = props;
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/_jsx(AccordionDetailsRoot, {
    className: clsx(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? AccordionDetails.propTypes /* remove-proptypes */ = {
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
export default AccordionDetails;