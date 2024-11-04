"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _propTypes = _interopRequireDefault(require("prop-types"));
var _Grid = require("@mui/system/Grid");
var _requirePropFactory = _interopRequireDefault(require("../utils/requirePropFactory"));
var _styles = require("../styles");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
/**
 *
 * Demos:
 *
 * - [Grid version 2](https://mui.com/material-ui/react-grid2/)
 *
 * API:
 *
 * - [Grid2 API](https://mui.com/material-ui/api/grid-2/)
 */
const Grid2 = (0, _Grid.createGrid)({
  createStyledComponent: (0, _styles.styled)('div', {
    name: 'MuiGrid2',
    slot: 'Root',
    overridesResolver: (props, styles) => styles.root
  }),
  componentName: 'MuiGrid2',
  useThemeProps: inProps => (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiGrid2'
  })
});
process.env.NODE_ENV !== "production" ? Grid2.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │ To update them, edit the TypeScript types and run `pnpm proptypes`. │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
   */
  children: _propTypes.default.node,
  /**
   * The number of columns.
   * @default 12
   */
  columns: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.arrayOf(_propTypes.default.number), _propTypes.default.number, _propTypes.default.object]),
  /**
   * Defines the horizontal space between the type `item` components.
   * It overrides the value of the `spacing` prop.
   */
  columnSpacing: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.string])), _propTypes.default.number, _propTypes.default.object, _propTypes.default.string]),
  /**
   * If `true`, the component will have the flex *container* behavior.
   * You should be wrapping *items* with a *container*.
   * @default false
   */
  container: _propTypes.default.bool,
  /**
   * Defines the `flex-direction` style property.
   * It is applied for all screen sizes.
   * @default 'row'
   */
  direction: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['column-reverse', 'column', 'row-reverse', 'row']), _propTypes.default.arrayOf(_propTypes.default.oneOf(['column-reverse', 'column', 'row-reverse', 'row'])), _propTypes.default.object]),
  /**
   * Defines the offset value for the type `item` components.
   */
  offset: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.string, _propTypes.default.number, _propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.string, _propTypes.default.number])), _propTypes.default.object]),
  /**
   * Defines the vertical space between the type `item` components.
   * It overrides the value of the `spacing` prop.
   */
  rowSpacing: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.string])), _propTypes.default.number, _propTypes.default.object, _propTypes.default.string]),
  /**
   * Defines the size of the the type `item` components.
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.string, _propTypes.default.bool, _propTypes.default.number, _propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.string, _propTypes.default.bool, _propTypes.default.number])), _propTypes.default.object]),
  /**
   * Defines the space between the type `item` components.
   * It can only be used on a type `container` component.
   * @default 0
   */
  spacing: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.string])), _propTypes.default.number, _propTypes.default.object, _propTypes.default.string]),
  /**
   * @ignore
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * @internal
   * The level of the grid starts from `0` and increases when the grid nests
   * inside another grid. Nesting is defined as a container Grid being a direct
   * child of a container Grid.
   *
   * ```js
   * <Grid container> // level 0
   *   <Grid container> // level 1
   *     <Grid container> // level 2
   * ```
   *
   * Only consecutive grid is considered nesting. A grid container will start at
   * `0` if there are non-Grid container element above it.
   *
   * ```js
   * <Grid container> // level 0
   *   <div>
   *     <Grid container> // level 0
   * ```
   *
   * ```js
   * <Grid container> // level 0
   *   <Grid>
   *     <Grid container> // level 0
   * ```
   */
  unstable_level: _propTypes.default.number,
  /**
   * Defines the `flex-wrap` style property.
   * It's applied for all screen sizes.
   * @default 'wrap'
   */
  wrap: _propTypes.default.oneOf(['nowrap', 'wrap-reverse', 'wrap'])
} : void 0;
if (process.env.NODE_ENV !== 'production') {
  const Component = Grid2;
  const requireProp = (0, _requirePropFactory.default)('Grid2', Component);
  // eslint-disable-next-line no-useless-concat
  Component['propTypes' + ''] = {
    // eslint-disable-next-line react/forbid-foreign-prop-types
    ...Component.propTypes,
    direction: requireProp('container'),
    spacing: requireProp('container'),
    wrap: requireProp('container')
  };
}
var _default = exports.default = Grid2;