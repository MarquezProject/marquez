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
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _svgIconClasses = require("./svgIconClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    color,
    fontSize,
    classes
  } = ownerState;
  const slots = {
    root: ['root', color !== 'inherit' && `color${(0, _capitalize.default)(color)}`, `fontSize${(0, _capitalize.default)(fontSize)}`]
  };
  return (0, _composeClasses.default)(slots, _svgIconClasses.getSvgIconUtilityClass, classes);
};
const SvgIconRoot = (0, _zeroStyled.styled)('svg', {
  name: 'MuiSvgIcon',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.color !== 'inherit' && styles[`color${(0, _capitalize.default)(ownerState.color)}`], styles[`fontSize${(0, _capitalize.default)(ownerState.fontSize)}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  userSelect: 'none',
  width: '1em',
  height: '1em',
  display: 'inline-block',
  flexShrink: 0,
  transition: theme.transitions?.create?.('fill', {
    duration: (theme.vars ?? theme).transitions?.duration?.shorter
  }),
  variants: [{
    props: props => !props.hasSvgAsChild,
    style: {
      // the <svg> will define the property that has `currentColor`
      // for example heroicons uses fill="none" and stroke="currentColor"
      fill: 'currentColor'
    }
  }, {
    props: {
      fontSize: 'inherit'
    },
    style: {
      fontSize: 'inherit'
    }
  }, {
    props: {
      fontSize: 'small'
    },
    style: {
      fontSize: theme.typography?.pxToRem?.(20) || '1.25rem'
    }
  }, {
    props: {
      fontSize: 'medium'
    },
    style: {
      fontSize: theme.typography?.pxToRem?.(24) || '1.5rem'
    }
  }, {
    props: {
      fontSize: 'large'
    },
    style: {
      fontSize: theme.typography?.pxToRem?.(35) || '2.1875rem'
    }
  },
  // TODO v5 deprecate color prop, v6 remove for sx
  ...Object.entries((theme.vars ?? theme).palette).filter(([, value]) => value && value.main).map(([color]) => ({
    props: {
      color
    },
    style: {
      color: (theme.vars ?? theme).palette?.[color]?.main
    }
  })), {
    props: {
      color: 'action'
    },
    style: {
      color: (theme.vars ?? theme).palette?.action?.active
    }
  }, {
    props: {
      color: 'disabled'
    },
    style: {
      color: (theme.vars ?? theme).palette?.action?.disabled
    }
  }, {
    props: {
      color: 'inherit'
    },
    style: {
      color: undefined
    }
  }]
})));
const SvgIcon = /*#__PURE__*/React.forwardRef(function SvgIcon(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiSvgIcon'
  });
  const {
    children,
    className,
    color = 'inherit',
    component = 'svg',
    fontSize = 'medium',
    htmlColor,
    inheritViewBox = false,
    titleAccess,
    viewBox = '0 0 24 24',
    ...other
  } = props;
  const hasSvgAsChild = /*#__PURE__*/React.isValidElement(children) && children.type === 'svg';
  const ownerState = {
    ...props,
    color,
    component,
    fontSize,
    instanceFontSize: inProps.fontSize,
    inheritViewBox,
    viewBox,
    hasSvgAsChild
  };
  const more = {};
  if (!inheritViewBox) {
    more.viewBox = viewBox;
  }
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(SvgIconRoot, {
    as: component,
    className: (0, _clsx.default)(classes.root, className),
    focusable: "false",
    color: htmlColor,
    "aria-hidden": titleAccess ? undefined : true,
    role: titleAccess ? 'img' : undefined,
    ref: ref,
    ...more,
    ...other,
    ...(hasSvgAsChild && children.props),
    ownerState: ownerState,
    children: [hasSvgAsChild ? children.props.children : children, titleAccess ? /*#__PURE__*/(0, _jsxRuntime.jsx)("title", {
      children: titleAccess
    }) : null]
  });
});
process.env.NODE_ENV !== "production" ? SvgIcon.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Node passed into the SVG element.
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
   * The color of the component.
   * It supports both default and custom theme colors, which can be added as shown in the
   * [palette customization guide](https://mui.com/material-ui/customization/palette/#custom-colors).
   * You can use the `htmlColor` prop to apply a color attribute to the SVG element.
   * @default 'inherit'
   */
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['inherit', 'action', 'disabled', 'primary', 'secondary', 'error', 'info', 'success', 'warning']), _propTypes.default.string]),
  /**
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * The fontSize applied to the icon. Defaults to 24px, but can be configure to inherit font size.
   * @default 'medium'
   */
  fontSize: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['inherit', 'large', 'medium', 'small']), _propTypes.default.string]),
  /**
   * Applies a color attribute to the SVG element.
   */
  htmlColor: _propTypes.default.string,
  /**
   * If `true`, the root node will inherit the custom `component`'s viewBox and the `viewBox`
   * prop will be ignored.
   * Useful when you want to reference a custom `component` and have `SvgIcon` pass that
   * `component`'s viewBox to the root node.
   * @default false
   */
  inheritViewBox: _propTypes.default.bool,
  /**
   * The shape-rendering attribute. The behavior of the different options is described on the
   * [MDN Web Docs](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/shape-rendering).
   * If you are having issues with blurry icons you should investigate this prop.
   */
  shapeRendering: _propTypes.default.string,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * Provides a human-readable title for the element that contains it.
   * https://www.w3.org/TR/SVG-access/#Equivalent
   */
  titleAccess: _propTypes.default.string,
  /**
   * Allows you to redefine what the coordinates without units mean inside an SVG element.
   * For example, if the SVG element is 500 (width) by 200 (height),
   * and you pass viewBox="0 0 50 20",
   * this means that the coordinates inside the SVG will go from the top left corner (0,0)
   * to bottom right (50,20) and each unit will be worth 10px.
   * @default '0 0 24 24'
   */
  viewBox: _propTypes.default.string
} : void 0;
if (SvgIcon) {
  SvgIcon.muiName = 'SvgIcon';
}
var _default = exports.default = SvgIcon;