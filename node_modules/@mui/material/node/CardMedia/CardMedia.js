"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _clsx = _interopRequireDefault(require("clsx"));
var _chainPropTypes = _interopRequireDefault(require("@mui/utils/chainPropTypes"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _cardMediaClasses = require("./cardMediaClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    isMediaComponent,
    isImageComponent
  } = ownerState;
  const slots = {
    root: ['root', isMediaComponent && 'media', isImageComponent && 'img']
  };
  return (0, _composeClasses.default)(slots, _cardMediaClasses.getCardMediaUtilityClass, classes);
};
const CardMediaRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiCardMedia',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    const {
      isMediaComponent,
      isImageComponent
    } = ownerState;
    return [styles.root, isMediaComponent && styles.media, isImageComponent && styles.img];
  }
})({
  display: 'block',
  backgroundSize: 'cover',
  backgroundRepeat: 'no-repeat',
  backgroundPosition: 'center',
  variants: [{
    props: {
      isMediaComponent: true
    },
    style: {
      width: '100%'
    }
  }, {
    props: {
      isImageComponent: true
    },
    style: {
      objectFit: 'cover'
    }
  }]
});
const MEDIA_COMPONENTS = ['video', 'audio', 'picture', 'iframe', 'img'];
const IMAGE_COMPONENTS = ['picture', 'img'];
const CardMedia = /*#__PURE__*/React.forwardRef(function CardMedia(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiCardMedia'
  });
  const {
    children,
    className,
    component = 'div',
    image,
    src,
    style,
    ...other
  } = props;
  const isMediaComponent = MEDIA_COMPONENTS.includes(component);
  const composedStyle = !isMediaComponent && image ? {
    backgroundImage: `url("${image}")`,
    ...style
  } : style;
  const ownerState = {
    ...props,
    component,
    isMediaComponent,
    isImageComponent: IMAGE_COMPONENTS.includes(component)
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(CardMediaRoot, {
    className: (0, _clsx.default)(classes.root, className),
    as: component,
    role: !isMediaComponent && image ? 'img' : undefined,
    ref: ref,
    style: composedStyle,
    ownerState: ownerState,
    src: isMediaComponent ? image || src : undefined,
    ...other,
    children: children
  });
});
process.env.NODE_ENV !== "production" ? CardMedia.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
   */
  children: (0, _chainPropTypes.default)(_propTypes.default.node, props => {
    if (!props.children && !props.image && !props.src && !props.component) {
      return new Error('MUI: Either `children`, `image`, `src` or `component` prop must be specified.');
    }
    return null;
  }),
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * Image to be displayed as a background image.
   * Either `image` or `src` prop must be specified.
   * Note that caller must specify height otherwise the image will not be visible.
   */
  image: _propTypes.default.string,
  /**
   * An alias for `image` property.
   * Available only with media components.
   * Media components: `video`, `audio`, `picture`, `iframe`, `img`.
   */
  src: _propTypes.default.string,
  /**
   * @ignore
   */
  style: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = CardMedia;