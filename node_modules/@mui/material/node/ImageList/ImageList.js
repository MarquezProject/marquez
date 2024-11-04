"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _integerPropType = _interopRequireDefault(require("@mui/utils/integerPropType"));
var _clsx = _interopRequireDefault(require("clsx"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var React = _interopRequireWildcard(require("react"));
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _imageListClasses = require("./imageListClasses");
var _ImageListContext = _interopRequireDefault(require("./ImageListContext"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    variant
  } = ownerState;
  const slots = {
    root: ['root', variant]
  };
  return (0, _composeClasses.default)(slots, _imageListClasses.getImageListUtilityClass, classes);
};
const ImageListRoot = (0, _zeroStyled.styled)('ul', {
  name: 'MuiImageList',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[ownerState.variant]];
  }
})({
  display: 'grid',
  overflowY: 'auto',
  listStyle: 'none',
  padding: 0,
  // Add iOS momentum scrolling for iOS < 13.0
  WebkitOverflowScrolling: 'touch',
  variants: [{
    props: {
      variant: 'masonry'
    },
    style: {
      display: 'block'
    }
  }]
});
const ImageList = /*#__PURE__*/React.forwardRef(function ImageList(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiImageList'
  });
  const {
    children,
    className,
    cols = 2,
    component = 'ul',
    rowHeight = 'auto',
    gap = 4,
    style: styleProp,
    variant = 'standard',
    ...other
  } = props;
  const contextValue = React.useMemo(() => ({
    rowHeight,
    gap,
    variant
  }), [rowHeight, gap, variant]);
  const style = variant === 'masonry' ? {
    columnCount: cols,
    columnGap: gap,
    ...styleProp
  } : {
    gridTemplateColumns: `repeat(${cols}, 1fr)`,
    gap,
    ...styleProp
  };
  const ownerState = {
    ...props,
    component,
    gap,
    rowHeight,
    variant
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(ImageListRoot, {
    as: component,
    className: (0, _clsx.default)(classes.root, classes[variant], className),
    ref: ref,
    style: style,
    ownerState: ownerState,
    ...other,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(_ImageListContext.default.Provider, {
      value: contextValue,
      children: children
    })
  });
});
process.env.NODE_ENV !== "production" ? ImageList.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component, normally `ImageListItem`s.
   */
  children: _propTypes.default /* @typescript-to-proptypes-ignore */.node.isRequired,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * Number of columns.
   * @default 2
   */
  cols: _integerPropType.default,
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * The gap between items in px.
   * @default 4
   */
  gap: _propTypes.default.number,
  /**
   * The height of one row in px.
   * @default 'auto'
   */
  rowHeight: _propTypes.default.oneOfType([_propTypes.default.oneOf(['auto']), _propTypes.default.number]),
  /**
   * @ignore
   */
  style: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The variant to use.
   * @default 'standard'
   */
  variant: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['masonry', 'quilted', 'standard', 'woven']), _propTypes.default.string])
} : void 0;
var _default = exports.default = ImageList;