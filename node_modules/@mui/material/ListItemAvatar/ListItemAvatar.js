'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import composeClasses from '@mui/utils/composeClasses';
import ListContext from "../List/ListContext.js";
import { styled } from "../zero-styled/index.js";
import { useDefaultProps } from "../DefaultPropsProvider/index.js";
import { getListItemAvatarUtilityClass } from "./listItemAvatarClasses.js";
import { jsx as _jsx } from "react/jsx-runtime";
const useUtilityClasses = ownerState => {
  const {
    alignItems,
    classes
  } = ownerState;
  const slots = {
    root: ['root', alignItems === 'flex-start' && 'alignItemsFlexStart']
  };
  return composeClasses(slots, getListItemAvatarUtilityClass, classes);
};
const ListItemAvatarRoot = styled('div', {
  name: 'MuiListItemAvatar',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.alignItems === 'flex-start' && styles.alignItemsFlexStart];
  }
})({
  minWidth: 56,
  flexShrink: 0,
  variants: [{
    props: {
      alignItems: 'flex-start'
    },
    style: {
      marginTop: 8
    }
  }]
});

/**
 * A simple wrapper to apply `List` styles to an `Avatar`.
 */
const ListItemAvatar = /*#__PURE__*/React.forwardRef(function ListItemAvatar(inProps, ref) {
  const props = useDefaultProps({
    props: inProps,
    name: 'MuiListItemAvatar'
  });
  const {
    className,
    ...other
  } = props;
  const context = React.useContext(ListContext);
  const ownerState = {
    ...props,
    alignItems: context.alignItems
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/_jsx(ListItemAvatarRoot, {
    className: clsx(classes.root, className),
    ownerState: ownerState,
    ref: ref,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? ListItemAvatar.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component, normally an `Avatar`.
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
export default ListItemAvatar;