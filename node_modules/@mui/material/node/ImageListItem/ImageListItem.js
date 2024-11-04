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
var _reactIs = require("react-is");
var _ImageListContext = _interopRequireDefault(require("../ImageList/ImageListContext"));
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _isMuiElement = _interopRequireDefault(require("../utils/isMuiElement"));
var _imageListItemClasses = _interopRequireWildcard(require("./imageListItemClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    variant
  } = ownerState;
  const slots = {
    root: ['root', variant],
    img: ['img']
  };
  return (0, _composeClasses.default)(slots, _imageListItemClasses.getImageListItemUtilityClass, classes);
};
const ImageListItemRoot = (0, _zeroStyled.styled)('li', {
  name: 'MuiImageListItem',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [{
      [`& .${_imageListItemClasses.default.img}`]: styles.img
    }, styles.root, styles[ownerState.variant]];
  }
})({
  display: 'block',
  position: 'relative',
  [`& .${_imageListItemClasses.default.img}`]: {
    objectFit: 'cover',
    width: '100%',
    height: '100%',
    display: 'block'
  },
  variants: [{
    props: {
      variant: 'standard'
    },
    style: {
      // For titlebar under list item
      display: 'flex',
      flexDirection: 'column'
    }
  }, {
    props: {
      variant: 'woven'
    },
    style: {
      height: '100%',
      alignSelf: 'center',
      '&:nth-of-type(even)': {
        height: '70%'
      }
    }
  }, {
    props: {
      variant: 'standard'
    },
    style: {
      [`& .${_imageListItemClasses.default.img}`]: {
        height: 'auto',
        flexGrow: 1
      }
    }
  }]
});
const ImageListItem = /*#__PURE__*/React.forwardRef(function ImageListItem(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiImageListItem'
  });

  // TODO: - Use jsdoc @default?: "cols rows default values are for docs only"
  const {
    children,
    className,
    cols = 1,
    component = 'li',
    rows = 1,
    style,
    ...other
  } = props;
  const {
    rowHeight = 'auto',
    gap,
    variant
  } = React.useContext(_ImageListContext.default);
  let height = 'auto';
  if (variant === 'woven') {
    height = undefined;
  } else if (rowHeight !== 'auto') {
    height = rowHeight * rows + gap * (rows - 1);
  }
  const ownerState = {
    ...props,
    cols,
    component,
    gap,
    rowHeight,
    rows,
    variant
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(ImageListItemRoot, {
    as: component,
    className: (0, _clsx.default)(classes.root, classes[variant], className),
    ref: ref,
    style: {
      height,
      gridColumnEnd: variant !== 'masonry' ? `span ${cols}` : undefined,
      gridRowEnd: variant !== 'masonry' ? `span ${rows}` : undefined,
      marginBottom: variant === 'masonry' ? gap : undefined,
      breakInside: variant === 'masonry' ? 'avoid' : undefined,
      ...style
    },
    ownerState: ownerState,
    ...other,
    children: React.Children.map(children, child => {
      if (! /*#__PURE__*/React.isValidElement(child)) {
        return null;
      }
      if (process.env.NODE_ENV !== 'production') {
        if ((0, _reactIs.isFragment)(child)) {
          console.error(["MUI: The ImageListItem component doesn't accept a Fragment as a child.", 'Consider providing an array instead.'].join('\n'));
        }
      }
      if (child.type === 'img' || (0, _isMuiElement.default)(child, ['Image'])) {
        return /*#__PURE__*/React.cloneElement(child, {
          className: (0, _clsx.default)(classes.img, child.props.className)
        });
      }
      return child;
    })
  });
});
process.env.NODE_ENV !== "production" ? ImageListItem.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component, normally an `<img>`.
   */
  children: _propTypes.default.node,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * Width of the item in number of grid columns.
   * @default 1
   */
  cols: _integerPropType.default,
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * Height of the item in number of grid rows.
   * @default 1
   */
  rows: _integerPropType.default,
  /**
   * @ignore
   */
  style: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = ImageListItem;