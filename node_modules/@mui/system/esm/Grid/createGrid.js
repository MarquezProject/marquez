import * as React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import isMuiElement from '@mui/utils/isMuiElement';
import generateUtilityClass from '@mui/utils/generateUtilityClass';
import composeClasses from '@mui/utils/composeClasses';
import systemStyled from "../styled/index.js";
import useThemePropsSystem from "../useThemeProps/index.js";
import useTheme from "../useTheme/index.js";
import { extendSxProp } from "../styleFunctionSx/index.js";
import createTheme from "../createTheme/index.js";
import { generateGridStyles, generateGridSizeStyles, generateGridColumnsStyles, generateGridColumnSpacingStyles, generateGridRowSpacingStyles, generateGridDirectionStyles, generateGridOffsetStyles, generateSizeClassNames, generateSpacingClassNames, generateDirectionClasses } from "./gridGenerator.js";
import { jsx as _jsx } from "react/jsx-runtime";
const defaultTheme = createTheme();

// widening Theme to any so that the consumer can own the theme structure.
const defaultCreateStyledComponent = systemStyled('div', {
  name: 'MuiGrid',
  slot: 'Root',
  overridesResolver: (props, styles) => styles.root
});
function useThemePropsDefault(props) {
  return useThemePropsSystem({
    props,
    name: 'MuiGrid',
    defaultTheme
  });
}
export default function createGrid(options = {}) {
  const {
    // This will allow adding custom styled fn (for example for custom sx style function)
    createStyledComponent = defaultCreateStyledComponent,
    useThemeProps = useThemePropsDefault,
    componentName = 'MuiGrid'
  } = options;
  const useUtilityClasses = (ownerState, theme) => {
    const {
      container,
      direction,
      spacing,
      wrap,
      size
    } = ownerState;
    const slots = {
      root: ['root', container && 'container', wrap !== 'wrap' && `wrap-xs-${String(wrap)}`, ...generateDirectionClasses(direction), ...generateSizeClassNames(size), ...(container ? generateSpacingClassNames(spacing, theme.breakpoints.keys[0]) : [])]
    };
    return composeClasses(slots, slot => generateUtilityClass(componentName, slot), {});
  };
  function parseResponsiveProp(propValue, breakpoints, shouldUseValue = () => true) {
    const parsedProp = {};
    if (propValue === null) {
      return parsedProp;
    }
    if (Array.isArray(propValue)) {
      propValue.forEach((value, index) => {
        if (value !== null && shouldUseValue(value) && breakpoints.keys[index]) {
          parsedProp[breakpoints.keys[index]] = value;
        }
      });
    } else if (typeof propValue === 'object') {
      Object.keys(propValue).forEach(key => {
        const value = propValue[key];
        if (value !== null && value !== undefined && shouldUseValue(value)) {
          parsedProp[key] = value;
        }
      });
    } else {
      parsedProp[breakpoints.keys[0]] = propValue;
    }
    return parsedProp;
  }
  const GridRoot = createStyledComponent(generateGridColumnsStyles, generateGridColumnSpacingStyles, generateGridRowSpacingStyles, generateGridSizeStyles, generateGridDirectionStyles, generateGridStyles, generateGridOffsetStyles);
  const Grid = /*#__PURE__*/React.forwardRef(function Grid(inProps, ref) {
    const theme = useTheme();
    const themeProps = useThemeProps(inProps);
    const props = extendSxProp(themeProps); // `color` type conflicts with html color attribute.
    const {
      className,
      children,
      columns: columnsProp = 12,
      container = false,
      component = 'div',
      direction = 'row',
      wrap = 'wrap',
      size: sizeProp = {},
      offset: offsetProp = {},
      spacing: spacingProp = 0,
      rowSpacing: rowSpacingProp = spacingProp,
      columnSpacing: columnSpacingProp = spacingProp,
      unstable_level: level = 0,
      ...other
    } = props;
    const size = parseResponsiveProp(sizeProp, theme.breakpoints, val => val !== false);
    const offset = parseResponsiveProp(offsetProp, theme.breakpoints);
    const columns = inProps.columns ?? (level ? undefined : columnsProp);
    const spacing = inProps.spacing ?? (level ? undefined : spacingProp);
    const rowSpacing = inProps.rowSpacing ?? inProps.spacing ?? (level ? undefined : rowSpacingProp);
    const columnSpacing = inProps.columnSpacing ?? inProps.spacing ?? (level ? undefined : columnSpacingProp);
    const ownerState = {
      ...props,
      level,
      columns,
      container,
      direction,
      wrap,
      spacing,
      rowSpacing,
      columnSpacing,
      size,
      offset
    };
    const classes = useUtilityClasses(ownerState, theme);
    return /*#__PURE__*/_jsx(GridRoot, {
      ref: ref,
      as: component,
      ownerState: ownerState,
      className: clsx(classes.root, className),
      ...other,
      children: React.Children.map(children, child => {
        if (/*#__PURE__*/React.isValidElement(child) && isMuiElement(child, ['Grid']) && container && child.props.container) {
          return /*#__PURE__*/React.cloneElement(child, {
            unstable_level: child.props?.unstable_level ?? level + 1
          });
        }
        return child;
      })
    });
  });
  process.env.NODE_ENV !== "production" ? Grid.propTypes /* remove-proptypes */ = {
    children: PropTypes.node,
    className: PropTypes.string,
    columns: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.number), PropTypes.number, PropTypes.object]),
    columnSpacing: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.number, PropTypes.string])), PropTypes.number, PropTypes.object, PropTypes.string]),
    component: PropTypes.elementType,
    container: PropTypes.bool,
    direction: PropTypes.oneOfType([PropTypes.oneOf(['column-reverse', 'column', 'row-reverse', 'row']), PropTypes.arrayOf(PropTypes.oneOf(['column-reverse', 'column', 'row-reverse', 'row'])), PropTypes.object]),
    offset: PropTypes.oneOfType([PropTypes.string, PropTypes.number, PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.string, PropTypes.number])), PropTypes.object]),
    rowSpacing: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.number, PropTypes.string])), PropTypes.number, PropTypes.object, PropTypes.string]),
    size: PropTypes.oneOfType([PropTypes.string, PropTypes.bool, PropTypes.number, PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.string, PropTypes.bool, PropTypes.number])), PropTypes.object]),
    spacing: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.number, PropTypes.string])), PropTypes.number, PropTypes.object, PropTypes.string]),
    sx: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.func, PropTypes.object, PropTypes.bool])), PropTypes.func, PropTypes.object]),
    wrap: PropTypes.oneOf(['nowrap', 'wrap-reverse', 'wrap'])
  } : void 0;

  // @ts-ignore internal logic for nested grid
  Grid.muiName = 'Grid';
  return Grid;
}