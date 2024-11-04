'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import composeClasses from '@mui/utils/composeClasses';
import { styled } from "../zero-styled/index.js";
import { useDefaultProps } from "../DefaultPropsProvider/index.js";
import { getCardActionsUtilityClass } from "./cardActionsClasses.js";
import { jsx as _jsx } from "react/jsx-runtime";
const useUtilityClasses = ownerState => {
  const {
    classes,
    disableSpacing
  } = ownerState;
  const slots = {
    root: ['root', !disableSpacing && 'spacing']
  };
  return composeClasses(slots, getCardActionsUtilityClass, classes);
};
const CardActionsRoot = styled('div', {
  name: 'MuiCardActions',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, !ownerState.disableSpacing && styles.spacing];
  }
})({
  display: 'flex',
  alignItems: 'center',
  padding: 8,
  variants: [{
    props: {
      disableSpacing: false
    },
    style: {
      '& > :not(style) ~ :not(style)': {
        marginLeft: 8
      }
    }
  }]
});
const CardActions = /*#__PURE__*/React.forwardRef(function CardActions(inProps, ref) {
  const props = useDefaultProps({
    props: inProps,
    name: 'MuiCardActions'
  });
  const {
    disableSpacing = false,
    className,
    ...other
  } = props;
  const ownerState = {
    ...props,
    disableSpacing
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/_jsx(CardActionsRoot, {
    className: clsx(classes.root, className),
    ownerState: ownerState,
    ref: ref,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? CardActions.propTypes /* remove-proptypes */ = {
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
   * If `true`, the actions do not have additional margin.
   * @default false
   */
  disableSpacing: PropTypes.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.func, PropTypes.object, PropTypes.bool])), PropTypes.func, PropTypes.object])
} : void 0;
export default CardActions;