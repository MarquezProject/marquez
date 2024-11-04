'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import composeClasses from '@mui/utils/composeClasses';
import { styled } from "../zero-styled/index.js";
import { useDefaultProps } from "../DefaultPropsProvider/index.js";
import ListContext from "../List/ListContext.js";
import { getListItemSecondaryActionClassesUtilityClass } from "./listItemSecondaryActionClasses.js";
import { jsx as _jsx } from "react/jsx-runtime";
const useUtilityClasses = ownerState => {
  const {
    disableGutters,
    classes
  } = ownerState;
  const slots = {
    root: ['root', disableGutters && 'disableGutters']
  };
  return composeClasses(slots, getListItemSecondaryActionClassesUtilityClass, classes);
};
const ListItemSecondaryActionRoot = styled('div', {
  name: 'MuiListItemSecondaryAction',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.disableGutters && styles.disableGutters];
  }
})({
  position: 'absolute',
  right: 16,
  top: '50%',
  transform: 'translateY(-50%)',
  variants: [{
    props: ({
      ownerState
    }) => ownerState.disableGutters,
    style: {
      right: 0
    }
  }]
});

/**
 * Must be used as the last child of ListItem to function properly.
 *
 * @deprecated Use the `secondaryAction` prop in the `ListItem` component instead. This component will be removed in v7. See [Migrating from deprecated APIs](https://mui.com/material-ui/migration/migrating-from-deprecated-apis/) for more details.
 */
const ListItemSecondaryAction = /*#__PURE__*/React.forwardRef(function ListItemSecondaryAction(inProps, ref) {
  const props = useDefaultProps({
    props: inProps,
    name: 'MuiListItemSecondaryAction'
  });
  const {
    className,
    ...other
  } = props;
  const context = React.useContext(ListContext);
  const ownerState = {
    ...props,
    disableGutters: context.disableGutters
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/_jsx(ListItemSecondaryActionRoot, {
    className: clsx(classes.root, className),
    ownerState: ownerState,
    ref: ref,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? ListItemSecondaryAction.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component, normally an `IconButton` or selection control.
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
ListItemSecondaryAction.muiName = 'ListItemSecondaryAction';
export default ListItemSecondaryAction;