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
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _dialogContentClasses = require("./dialogContentClasses");
var _dialogTitleClasses = _interopRequireDefault(require("../DialogTitle/dialogTitleClasses"));
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    dividers
  } = ownerState;
  const slots = {
    root: ['root', dividers && 'dividers']
  };
  return (0, _composeClasses.default)(slots, _dialogContentClasses.getDialogContentUtilityClass, classes);
};
const DialogContentRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiDialogContent',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, ownerState.dividers && styles.dividers];
  }
})((0, _memoTheme.default)(({
  theme
}) => ({
  flex: '1 1 auto',
  // Add iOS momentum scrolling for iOS < 13.0
  WebkitOverflowScrolling: 'touch',
  overflowY: 'auto',
  padding: '20px 24px',
  variants: [{
    props: ({
      ownerState
    }) => ownerState.dividers,
    style: {
      padding: '16px 24px',
      borderTop: `1px solid ${(theme.vars || theme).palette.divider}`,
      borderBottom: `1px solid ${(theme.vars || theme).palette.divider}`
    }
  }, {
    props: ({
      ownerState
    }) => !ownerState.dividers,
    style: {
      [`.${_dialogTitleClasses.default.root} + &`]: {
        paddingTop: 0
      }
    }
  }]
})));
const DialogContent = /*#__PURE__*/React.forwardRef(function DialogContent(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiDialogContent'
  });
  const {
    className,
    dividers = false,
    ...other
  } = props;
  const ownerState = {
    ...props,
    dividers
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(DialogContentRoot, {
    className: (0, _clsx.default)(classes.root, className),
    ownerState: ownerState,
    ref: ref,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? DialogContent.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The content of the component.
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
   * Display the top and bottom dividers.
   * @default false
   */
  dividers: _propTypes.default.bool,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object])
} : void 0;
var _default = exports.default = DialogContent;