'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import composeClasses from '@mui/utils/composeClasses';
import TableContext from "./TableContext.js";
import { styled } from "../zero-styled/index.js";
import memoTheme from "../utils/memoTheme.js";
import { useDefaultProps } from "../DefaultPropsProvider/index.js";
import { getTableUtilityClass } from "./tableClasses.js";
import { jsx as _jsx } from "react/jsx-runtime";
const useUtilityClasses = ownerState => {
  const {
    classes,
    stickyHeader
  } = ownerState;
  const slots = {
    root: ['root', stickyHeader && 'stickyHeader']
  };
  return composeClasses(slots, getTableUtilityClass, classes);
};
const TableRoot = styled('table', {
  name: 'MuiTable',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.stickyHeader && styles.stickyHeader];
  }
})(memoTheme(({
  theme
}) => ({
  display: 'table',
  width: '100%',
  borderCollapse: 'collapse',
  borderSpacing: 0,
  '& caption': {
    ...theme.typography.body2,
    padding: theme.spacing(2),
    color: (theme.vars || theme).palette.text.secondary,
    textAlign: 'left',
    captionSide: 'bottom'
  },
  variants: [{
    props: ({
      ownerState
    }) => ownerState.stickyHeader,
    style: {
      borderCollapse: 'separate'
    }
  }]
})));
const defaultComponent = 'table';
const Table = /*#__PURE__*/React.forwardRef(function Table(inProps, ref) {
  const props = useDefaultProps({
    props: inProps,
    name: 'MuiTable'
  });
  const {
    className,
    component = defaultComponent,
    padding = 'normal',
    size = 'medium',
    stickyHeader = false,
    ...other
  } = props;
  const ownerState = {
    ...props,
    component,
    padding,
    size,
    stickyHeader
  };
  const classes = useUtilityClasses(ownerState);
  const table = React.useMemo(() => ({
    padding,
    size,
    stickyHeader
  }), [padding, size, stickyHeader]);
  return /*#__PURE__*/_jsx(TableContext.Provider, {
    value: table,
    children: /*#__PURE__*/_jsx(TableRoot, {
      as: component,
      role: component === defaultComponent ? null : 'table',
      ref: ref,
      className: clsx(classes.root, className),
      ownerState: ownerState,
      ...other
    })
  });
});
process.env.NODE_ENV !== "production" ? Table.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the table, normally `TableHead` and `TableBody`.
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
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: PropTypes.elementType,
  /**
   * Allows TableCells to inherit padding of the Table.
   * @default 'normal'
   */
  padding: PropTypes.oneOf(['checkbox', 'none', 'normal']),
  /**
   * Allows TableCells to inherit size of the Table.
   * @default 'medium'
   */
  size: PropTypes /* @typescript-to-proptypes-ignore */.oneOfType([PropTypes.oneOf(['medium', 'small']), PropTypes.string]),
  /**
   * Set the header sticky.
   * @default false
   */
  stickyHeader: PropTypes.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.func, PropTypes.object, PropTypes.bool])), PropTypes.func, PropTypes.object])
} : void 0;
export default Table;