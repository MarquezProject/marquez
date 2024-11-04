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
var _integerPropType = _interopRequireDefault(require("@mui/utils/integerPropType"));
var _paginationClasses = require("./paginationClasses");
var _usePagination = _interopRequireDefault(require("../usePagination"));
var _PaginationItem = _interopRequireDefault(require("../PaginationItem"));
var _zeroStyled = require("../zero-styled");
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    variant
  } = ownerState;
  const slots = {
    root: ['root', variant],
    ul: ['ul']
  };
  return (0, _composeClasses.default)(slots, _paginationClasses.getPaginationUtilityClass, classes);
};
const PaginationRoot = (0, _zeroStyled.styled)('nav', {
  name: 'MuiPagination',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[ownerState.variant]];
  }
})({});
const PaginationUl = (0, _zeroStyled.styled)('ul', {
  name: 'MuiPagination',
  slot: 'Ul',
  overridesResolver: (props, styles) => styles.ul
})({
  display: 'flex',
  flexWrap: 'wrap',
  alignItems: 'center',
  padding: 0,
  margin: 0,
  listStyle: 'none'
});
function defaultGetAriaLabel(type, page, selected) {
  if (type === 'page') {
    return `${selected ? '' : 'Go to '}page ${page}`;
  }
  return `Go to ${type} page`;
}
const Pagination = /*#__PURE__*/React.forwardRef(function Pagination(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiPagination'
  });
  const {
    boundaryCount = 1,
    className,
    color = 'standard',
    count = 1,
    defaultPage = 1,
    disabled = false,
    getItemAriaLabel = defaultGetAriaLabel,
    hideNextButton = false,
    hidePrevButton = false,
    onChange,
    page,
    renderItem = item => /*#__PURE__*/(0, _jsxRuntime.jsx)(_PaginationItem.default, {
      ...item
    }),
    shape = 'circular',
    showFirstButton = false,
    showLastButton = false,
    siblingCount = 1,
    size = 'medium',
    variant = 'text',
    ...other
  } = props;
  const {
    items
  } = (0, _usePagination.default)({
    ...props,
    componentName: 'Pagination'
  });
  const ownerState = {
    ...props,
    boundaryCount,
    color,
    count,
    defaultPage,
    disabled,
    getItemAriaLabel,
    hideNextButton,
    hidePrevButton,
    renderItem,
    shape,
    showFirstButton,
    showLastButton,
    siblingCount,
    size,
    variant
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(PaginationRoot, {
    "aria-label": "pagination navigation",
    className: (0, _clsx.default)(classes.root, className),
    ownerState: ownerState,
    ref: ref,
    ...other,
    children: /*#__PURE__*/(0, _jsxRuntime.jsx)(PaginationUl, {
      className: classes.ul,
      ownerState: ownerState,
      children: items.map((item, index) => /*#__PURE__*/(0, _jsxRuntime.jsx)("li", {
        children: renderItem({
          ...item,
          color,
          'aria-label': getItemAriaLabel(item.type, item.page, item.selected),
          shape,
          size,
          variant
        })
      }, index))
    })
  });
});

// @default tags synced with default values from usePagination

process.env.NODE_ENV !== "production" ? Pagination.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Number of always visible pages at the beginning and end.
   * @default 1
   */
  boundaryCount: _integerPropType.default,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: _propTypes.default.object,
  /**
   * @ignore
   */
  className: _propTypes.default.string,
  /**
   * The active color.
   * It supports both default and custom theme colors, which can be added as shown in the
   * [palette customization guide](https://mui.com/material-ui/customization/palette/#custom-colors).
   * @default 'standard'
   */
  color: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['primary', 'secondary', 'standard']), _propTypes.default.string]),
  /**
   * The total number of pages.
   * @default 1
   */
  count: _integerPropType.default,
  /**
   * The page selected by default when the component is uncontrolled.
   * @default 1
   */
  defaultPage: _integerPropType.default,
  /**
   * If `true`, the component is disabled.
   * @default false
   */
  disabled: _propTypes.default.bool,
  /**
   * Accepts a function which returns a string value that provides a user-friendly name for the current page.
   * This is important for screen reader users.
   *
   * For localization purposes, you can use the provided [translations](https://mui.com/material-ui/guides/localization/).
   * @param {string} type The link or button type to format ('page' | 'first' | 'last' | 'next' | 'previous' | 'start-ellipsis' | 'end-ellipsis'). Defaults to 'page'.
   * @param {number | null} page The page number to format.
   * @param {boolean} selected If true, the current page is selected.
   * @returns {string}
   */
  getItemAriaLabel: _propTypes.default.func,
  /**
   * If `true`, hide the next-page button.
   * @default false
   */
  hideNextButton: _propTypes.default.bool,
  /**
   * If `true`, hide the previous-page button.
   * @default false
   */
  hidePrevButton: _propTypes.default.bool,
  /**
   * Callback fired when the page is changed.
   *
   * @param {React.ChangeEvent<unknown>} event The event source of the callback.
   * @param {number} page The page selected.
   */
  onChange: _propTypes.default.func,
  /**
   * The current page. Unlike `TablePagination`, which starts numbering from `0`, this pagination starts from `1`.
   */
  page: _integerPropType.default,
  /**
   * Render the item.
   * @param {PaginationRenderItemParams} params The props to spread on a PaginationItem.
   * @returns {ReactNode}
   * @default (item) => <PaginationItem {...item} />
   */
  renderItem: _propTypes.default.func,
  /**
   * The shape of the pagination items.
   * @default 'circular'
   */
  shape: _propTypes.default.oneOf(['circular', 'rounded']),
  /**
   * If `true`, show the first-page button.
   * @default false
   */
  showFirstButton: _propTypes.default.bool,
  /**
   * If `true`, show the last-page button.
   * @default false
   */
  showLastButton: _propTypes.default.bool,
  /**
   * Number of always visible pages before and after the current page.
   * @default 1
   */
  siblingCount: _integerPropType.default,
  /**
   * The size of the component.
   * @default 'medium'
   */
  size: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['small', 'medium', 'large']), _propTypes.default.string]),
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The variant to use.
   * @default 'text'
   */
  variant: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['outlined', 'text']), _propTypes.default.string])
} : void 0;
var _default = exports.default = Pagination;