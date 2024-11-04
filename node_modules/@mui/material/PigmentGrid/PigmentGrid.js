import * as React from 'react';
import clsx from 'clsx';
import PropTypes from 'prop-types';
// @ts-ignore
import Grid from '@mui/material-pigment-css/Grid';
import composeClasses from '@mui/utils/composeClasses';
import generateUtilityClass from '@mui/utils/generateUtilityClass';
import { generateDirectionClasses, generateSizeClassNames, generateSpacingClassNames } from '@mui/system/Grid/gridGenerator';
import { jsx as _jsx } from "react/jsx-runtime";
const useUtilityClasses = ownerState => {
  const {
    container,
    direction,
    size,
    spacing
  } = ownerState;
  let gridSize = {};
  if (size) {
    if (Array.isArray(size)) {
      size.forEach((value, index) => {
        gridSize = {
          ...gridSize,
          [index]: value
        };
      });
    }
    if (typeof size === 'object') {
      gridSize = size;
    }
  }
  const slots = {
    root: ['root', container && 'container', ...generateDirectionClasses(direction), ...generateSizeClassNames(gridSize), ...(container ? generateSpacingClassNames(spacing) : [])]
  };
  return composeClasses(slots, slot => generateUtilityClass('MuiGrid2', slot), {});
};
/**
 *
 * Demos:
 *
 * - [Grid version 2](https://mui.com/material-ui/react-grid2/)
 *
 * API:
 *
 * - [PigmentGrid API](https://mui.com/material-ui/api/pigment-grid/)
 */
const PigmentGrid = /*#__PURE__*/React.forwardRef(function PigmentGrid(props, ref) {
  const {
    className,
    ...other
  } = props;
  const classes = useUtilityClasses(props);
  return /*#__PURE__*/_jsx(Grid, {
    ref: ref,
    className: clsx(classes.root, className),
    ...other
  });
});
process.env.NODE_ENV !== "production" ? PigmentGrid.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │ To update them, edit the TypeScript types and run `pnpm proptypes`. │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
   */
  children: PropTypes.node,
  /**
   * @ignore
   */
  className: PropTypes.string,
  /**
   * The number of columns.
   * @default 12
   */
  columns: PropTypes /* @typescript-to-proptypes-ignore */.oneOfType([PropTypes.arrayOf(PropTypes.number), PropTypes.number, PropTypes.object]),
  /**
   * Defines the horizontal space between the type `item` components.
   * It overrides the value of the `spacing` prop.
   */
  columnSpacing: PropTypes /* @typescript-to-proptypes-ignore */.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.number, PropTypes.string]).isRequired), PropTypes.number, PropTypes.object, PropTypes.string]),
  /**
   * If `true`, the component will have the flex *container* behavior.
   * You should be wrapping *items* with a *container*.
   * @default false
   */
  container: PropTypes.bool,
  /**
   * Defines the `flex-direction` style property.
   * It is applied for all screen sizes.
   * @default 'row'
   */
  direction: PropTypes /* @typescript-to-proptypes-ignore */.oneOfType([PropTypes.oneOf(['column', 'column-reverse', 'row', 'row-reverse']), PropTypes.arrayOf(PropTypes.oneOf(['column', 'column-reverse', 'row', 'row-reverse'])), PropTypes.object]),
  /**
   * Defines the offset of the grid.
   */
  offset: PropTypes /* @typescript-to-proptypes-ignore */.oneOfType([PropTypes.arrayOf(PropTypes.number), PropTypes.number, PropTypes.object]),
  /**
   * Defines the vertical space between the type `item` components.
   * It overrides the value of the `spacing` prop.
   */
  rowSpacing: PropTypes /* @typescript-to-proptypes-ignore */.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.number, PropTypes.string]).isRequired), PropTypes.number, PropTypes.object, PropTypes.string]),
  /**
   * Defines the column size of the grid.
   */
  size: PropTypes /* @typescript-to-proptypes-ignore */.oneOfType([PropTypes.arrayOf(PropTypes.number), PropTypes.number, PropTypes.object]),
  /**
   * Defines the space between the type `item` components.
   * It can only be used on a type `container` component.
   * @default 0
   */
  spacing: PropTypes /* @typescript-to-proptypes-ignore */.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.number, PropTypes.string]).isRequired), PropTypes.number, PropTypes.object, PropTypes.string]),
  /**
   * @ignore
   */
  sx: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.func, PropTypes.object, PropTypes.bool])), PropTypes.func, PropTypes.object]),
  /**
   * Defines the `flex-wrap` style property.
   * It's applied for all screen sizes.
   * @default 'wrap'
   */
  wrap: PropTypes.oneOf(['nowrap', 'wrap-reverse', 'wrap'])
} : void 0;

// @ts-ignore internal logic for nested grid
PigmentGrid.muiName = 'Grid';
export default PigmentGrid;