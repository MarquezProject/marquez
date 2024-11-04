"use strict";
'use client';

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _clsx = _interopRequireDefault(require("clsx"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var React = _interopRequireWildcard(require("react"));
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _capitalize = _interopRequireDefault(require("../utils/capitalize"));
var _imageListItemBarClasses = require("./imageListItemBarClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    position,
    actionIcon,
    actionPosition
  } = ownerState;
  const slots = {
    root: ['root', `position${(0, _capitalize.default)(position)}`, `actionPosition${(0, _capitalize.default)(actionPosition)}`],
    titleWrap: ['titleWrap', `titleWrap${(0, _capitalize.default)(position)}`, actionIcon && `titleWrapActionPos${(0, _capitalize.default)(actionPosition)}`],
    title: ['title'],
    subtitle: ['subtitle'],
    actionIcon: ['actionIcon', `actionIconActionPos${(0, _capitalize.default)(actionPosition)}`]
  };
  return (0, _composeClasses.default)(slots, _imageListItemBarClasses.getImageListItemBarUtilityClass, classes);
};
const ImageListItemBarRoot = (0, _zeroStyled.styled)('div', {
  name: 'MuiImageListItemBar',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[`position${(0, _capitalize.default)(ownerState.position)}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => {
  return {
    position: 'absolute',
    left: 0,
    right: 0,
    background: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    alignItems: 'center',
    fontFamily: theme.typography.fontFamily,
    variants: [{
      props: {
        position: 'bottom'
      },
      style: {
        bottom: 0
      }
    }, {
      props: {
        position: 'top'
      },
      style: {
        top: 0
      }
    }, {
      props: {
        position: 'below'
      },
      style: {
        position: 'relative',
        background: 'transparent',
        alignItems: 'normal'
      }
    }]
  };
}));
const ImageListItemBarTitleWrap = (0, _zeroStyled.styled)('div', {
  name: 'MuiImageListItemBar',
  slot: 'TitleWrap',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.titleWrap, styles[`titleWrap${(0, _capitalize.default)(ownerState.position)}`], ownerState.actionIcon && styles[`titleWrapActionPos${(0, _capitalize.default)(ownerState.actionPosition)}`]];
  }
})((0, _memoTheme.default)(({
  theme
}) => {
  return {
    flexGrow: 1,
    padding: '12px 16px',
    color: (theme.vars || theme).palette.common.white,
    overflow: 'hidden',
    variants: [{
      props: {
        position: 'below'
      },
      style: {
        padding: '6px 0 12px',
        color: 'inherit'
      }
    }, {
      props: ({
        ownerState
      }) => ownerState.actionIcon && ownerState.actionPosition === 'left',
      style: {
        paddingLeft: 0
      }
    }, {
      props: ({
        ownerState
      }) => ownerState.actionIcon && ownerState.actionPosition === 'right',
      style: {
        paddingRight: 0
      }
    }]
  };
}));
const ImageListItemBarTitle = (0, _zeroStyled.styled)('div', {
  name: 'MuiImageListItemBar',
  slot: 'Title',
  overridesResolver: (props, styles) => styles.title
})((0, _memoTheme.default)(({
  theme
}) => {
  return {
    fontSize: theme.typography.pxToRem(16),
    lineHeight: '24px',
    textOverflow: 'ellipsis',
    overflow: 'hidden',
    whiteSpace: 'nowrap'
  };
}));
const ImageListItemBarSubtitle = (0, _zeroStyled.styled)('div', {
  name: 'MuiImageListItemBar',
  slot: 'Subtitle',
  overridesResolver: (props, styles) => styles.subtitle
})((0, _memoTheme.default)(({
  theme
}) => {
  return {
    fontSize: theme.typography.pxToRem(12),
    lineHeight: 1,
    textOverflow: 'ellipsis',
    overflow: 'hidden',
    whiteSpace: 'nowrap'
  };
}));
const ImageListItemBarActionIcon = (0, _zeroStyled.styled)('div', {
  name: 'MuiImageListItemBar',
  slot: 'ActionIcon',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.actionIcon, styles[`actionIconActionPos${(0, _capitalize.default)(ownerState.actionPosition)}`]];
  }
})({
  variants: [{
    props: {
      actionPosition: 'left'
    },
    style: {
      order: -1
    }
  }]
});
const ImageListItemBar = /*#__PURE__*/React.forwardRef(function ImageListItemBar(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiImageListItemBar'
  });
  const {
    actionIcon,
    actionPosition = 'right',
    className,
    subtitle,
    title,
    position = 'bottom',
    ...other
  } = props;
  const ownerState = {
    ...props,
    position,
    actionPosition
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsxs)(ImageListItemBarRoot, {
    ownerState: ownerState,
    className: (0, _clsx.default)(classes.root, className),
    ref: ref,
    ...other,
    children: [/*#__PURE__*/(0, _jsxRuntime.jsxs)(ImageListItemBarTitleWrap, {
      ownerState: ownerState,
      className: classes.titleWrap,
      children: [/*#__PURE__*/(0, _jsxRuntime.jsx)(ImageListItemBarTitle, {
        className: classes.title,
        children: title
      }), subtitle ? /*#__PURE__*/(0, _jsxRuntime.jsx)(ImageListItemBarSubtitle, {
        className: classes.subtitle,
        children: subtitle
      }) : null]
    }), actionIcon ? /*#__PURE__*/(0, _jsxRuntime.jsx)(ImageListItemBarActionIcon, {
      ownerState: ownerState,
      className: classes.actionIcon,
      children: actionIcon
    }) : null]
  });
});
process.env.NODE_ENV !== "production" ? ImageListItemBar.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * An IconButton element to be used as secondary action target
   * (primary action target is the item itself).
   */
  actionIcon: _propTypes.default.node,
  /**
   * Position of secondary action IconButton.
   * @default 'right'
   */
  actionPosition: _propTypes.default.oneOf(['left', 'right']),
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
   * Position of the title bar.
   * @default 'bottom'
   */
  position: _propTypes.default.oneOf(['below', 'bottom', 'top']),
  /**
   * String or element serving as subtitle (support text).
   */
  subtitle: _propTypes.default.node,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * Title to be displayed.
   */
  title: _propTypes.default.node
} : void 0;
var _default = exports.default = ImageListItemBar;