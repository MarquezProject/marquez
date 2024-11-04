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
var _Typography = _interopRequireWildcard(require("../Typography"));
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _cardHeaderClasses = _interopRequireWildcard(require("./cardHeaderClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes
  } = ownerState;
  const slots = {
    root: ['root'],
    avatar: ['avatar'],
    action: ['action'],
    content: ['content'],
    title: ['title'],
    subheader: ['subheader']
  };
  return (0, _composeClasses.default)(slots, _cardHeaderClasses.getCardHeaderUtilityClass, classes);
};
const CardHeaderRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiCardHeader',
  slot: 'Root',
  overridesResolver: (props, styles) => ({
    [`& .${_cardHeaderClasses.default.title}`]: styles.title,
    [`& .${_cardHeaderClasses.default.subheader}`]: styles.subheader,
    ...styles.root
  })
})({
  display: 'flex',
  alignItems: 'center',
  padding: 16
});
const CardHeaderAvatar = (0, _zeroStyled.styled)('div', {
  name: 'MuiCardHeader',
  slot: 'Avatar',
  overridesResolver: (props, styles) => styles.avatar
})({
  display: 'flex',
  flex: '0 0 auto',
  marginRight: 16
});
const CardHeaderAction = (0, _zeroStyled.styled)('div', {
  name: 'MuiCardHeader',
  slot: 'Action',
  overridesResolver: (props, styles) => styles.action
})({
  flex: '0 0 auto',
  alignSelf: 'flex-start',
  marginTop: -4,
  marginRight: -8,
  marginBottom: -4
});
const CardHeaderContent = (0, _zeroStyled.styled)('div', {
  name: 'MuiCardHeader',
  slot: 'Content',
  overridesResolver: (props, styles) => styles.content
})({
  flex: '1 1 auto',
  [`.${_Typography.typographyClasses.root}:where(& .${_cardHeaderClasses.default.title})`]: {
    display: 'block'
  },
  [`.${_Typography.typographyClasses.root}:where(& .${_cardHeaderClasses.default.subheader})`]: {
    display: 'block'
  }
});
const CardHeader = /*#__PURE__*/React.forwardRef(function CardHeader(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiCardHeader'
  });
  const {
    action,
    avatar,
    className,
    component = 'div',
    disableTypography = false,
    subheader: subheaderProp,
    subheaderTypographyProps,
    title: titleProp,
    titleTypographyProps,
    ...other
  } = props;
  const ownerState = {
    ...props,
    component,
    disableTypography
  };
  const classes = useUtilityClasses(ownerState);
  let title = titleProp;
  if (title != null && title.type !== _Typography.default && !disableTypography) {
    title = /*#__PURE__*/(0, _jsxRuntime.jsx)(_Typography.default, {
      variant: avatar ? 'body2' : 'h5',
      className: classes.title,
      component: "span",
      ...titleTypographyProps,
      children: title
    });
  }
  let subheader = subheaderProp;
  if (subheader != null && subheader.type !== _Typography.default && !disableTypography) {
    subheader = /*#__PURE__*/(0, _jsxRuntime.jsx)(_Typography.default, {
      variant: avatar ? 'body2' : 'body1',
      className: classes.subheader,
      color: "textSecondary",
      component: "span",
      ...subheaderTypographyProps,
      children: subheader
    });
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(CardHeaderRoot, {
    className: (0, _clsx.default)(classes.root, className),
    as: component,
    ref: ref,
    ownerState: ownerState,
    ...other,
    children: [avatar && /*#__PURE__*/(0, _jsxRuntime.jsx)(CardHeaderAvatar, {
      className: classes.avatar,
      ownerState: ownerState,
      children: avatar
    }), /*#__PURE__*/(0, _jsxRuntime.jsxs)(CardHeaderContent, {
      className: classes.content,
      ownerState: ownerState,
      children: [title, subheader]
    }), action && /*#__PURE__*/(0, _jsxRuntime.jsx)(CardHeaderAction, {
      className: classes.action,
      ownerState: ownerState,
      children: action
    })]
  });
});
process.env.NODE_ENV !== "production" ? CardHeader.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The action to display in the card header.
   */
  action: _propTypes.default.node,
  /**
   * The Avatar element to display.
   */
  avatar: _propTypes.default.node,
  /**
   * @ignore
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
   * The component used for the root node.
   * Either a string to use a HTML element or a component.
   */
  component: _propTypes.default.elementType,
  /**
   * If `true`, `subheader` and `title` won't be wrapped by a Typography component.
   * This can be useful to render an alternative Typography variant by wrapping
   * the `title` text, and optional `subheader` text
   * with the Typography component.
   * @default false
   */
  disableTypography: _propTypes.default.bool,
  /**
   * The content of the component.
   */
  subheader: _propTypes.default.node,
  /**
   * These props will be forwarded to the subheader
   * (as long as disableTypography is not `true`).
   */
  subheaderTypographyProps: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The content of the component.
   */
  title: _propTypes.default.node,
  /**
   * These props will be forwarded to the title
   * (as long as disableTypography is not `true`).
   */
  titleTypographyProps: _propTypes.default.object
} : void 0;
var _default = exports.default = CardHeader;