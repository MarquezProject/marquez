"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _clsx = _interopRequireDefault(require("clsx"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _Grid = _interopRequireDefault(require("@mui/material-pigment-css/Grid"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _generateUtilityClass = _interopRequireDefault(require("@mui/utils/generateUtilityClass"));
var _gridGenerator = require("@mui/system/Grid/gridGenerator");
var _jsxRuntime = require("react/jsx-runtime");
// @ts-ignore

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
    root: ['root', container && 'container', ...(0, _gridGenerator.generateDirectionClasses)(direction), ...(0, _gridGenerator.generateSizeClassNames)(gridSize), ...(container ? (0, _gridGenerator.generateSpacingClassNames)(spacing) : [])]
  };
  return (0, _composeClasses.default)(slots, slot => (0, _generateUtilityClass.default)('MuiGrid2', slot), {});
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
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(_Grid.default, {
    ref: ref,
    className: (0, _clsx.default)(classes.root, className),
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
  children: _propTypes.default.node,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The number of columns.
   * @default 12
   */
  columns: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.arrayOf(_propTypes.default.number), _propTypes.default.number, _propTypes.default.object]),
  /**
   * Defines the horizontal space between the type `item` components.
   * It overrides the value of the `spacing` prop.
   */
  columnSpacing: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.string]).isRequired), _propTypes.default.number, _propTypes.default.object, _propTypes.default.string]),
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
  direction: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['column', 'column-reverse', 'row', 'row-reverse']), _propTypes.default.arrayOf(_propTypes.default.oneOf(['column', 'column-reverse', 'row', 'row-reverse'])), _propTypes.default.object]),
  /**
   * Defines the offset of the grid.
   */
  offset: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.arrayOf(_propTypes.default.number), _propTypes.default.number, _propTypes.default.object]),
  /**
   * Defines the vertical space between the type `item` components.
   * It overrides the value of the `spacing` prop.
   */
  rowSpacing: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.string]).isRequired), _propTypes.default.number, _propTypes.default.object, _propTypes.default.string]),
  /**
   * Defines the column size of the grid.
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.arrayOf(_propTypes.default.number), _propTypes.default.number, _propTypes.default.object]),
  /**
   * Defines the space between the type `item` components.
   * It can only be used on a type `container` component.
   * @default 0
   */
  spacing: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.string]).isRequired), _propTypes.default.number, _propTypes.default.object, _propTypes.default.string]),
  /**
   * @ignore
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * Defines the `flex-wrap` style property.
   * It's applied for all screen sizes.
   * @default 'wrap'
   */
  wrap: _propTypes.default.oneOf(['nowrap', 'wrap-reverse', 'wrap'])
} : void 0;

// @ts-ignore internal logic for nested grid
PigmentGrid.muiName = 'Grid';
var _default = exports.default = PigmentGrid;