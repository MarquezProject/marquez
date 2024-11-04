"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var _clsx = _interopRequireDefault(require("clsx"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _composeClasses = _interopRequireDefault(require("@mui/utils/composeClasses"));
var _styles = require("../styles");
var _zeroStyled = require("../zero-styled");
var _memoTheme = _interopRequireDefault(require("../utils/memoTheme"));
var _DefaultPropsProvider = require("../DefaultPropsProvider");
var _skeletonClasses = require("./skeletonClasses");
var _jsxRuntime = require("react/jsx-runtime");
const useUtilityClasses = ownerState => {
  const {
    classes,
    variant,
    animation,
    hasChildren,
    width,
    height
  } = ownerState;
  const slots = {
    root: ['root', variant, animation, hasChildren && 'withChildren', hasChildren && !width && 'fitContent', hasChildren && !height && 'heightAuto']
  };
  return (0, _composeClasses.default)(slots, _skeletonClasses.getSkeletonUtilityClass, classes);
};
const pulseKeyframe = (0, _zeroStyled.keyframes)`
  0% {
    opacity: 1;
  }

  50% {
    opacity: 0.4;
  }

  100% {
    opacity: 1;
  }
`;
const waveKeyframe = (0, _zeroStyled.keyframes)`
  0% {
    transform: translateX(-100%);
  }

  50% {
    /* +0.5s of delay between each loop */
    transform: translateX(100%);
  }

  100% {
    transform: translateX(100%);
  }
`;

// This implementation is for supporting both Styled-components v4+ and Pigment CSS.
// A global animation has to be created here for Styled-components v4+ (https://github.com/styled-components/styled-components/blob/main/packages/styled-components/src/utils/errors.md#12).
// which can be done by checking typeof indeterminate1Keyframe !== 'string' (at runtime, Pigment CSS transform keyframes`` to a string).
const pulseAnimation = typeof pulseKeyframe !== 'string' ? (0, _zeroStyled.css)`
        animation: ${pulseKeyframe} 2s ease-in-out 0.5s infinite;
      ` : null;
const waveAnimation = typeof waveKeyframe !== 'string' ? (0, _zeroStyled.css)`
        &::after {
          animation: ${waveKeyframe} 2s linear 0.5s infinite;
        }
      ` : null;
const SkeletonRoot = (0, _zeroStyled.styled)('span', {
  name: 'MuiSkeleton',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[ownerState.variant], ownerState.animation !== false && styles[ownerState.animation], ownerState.hasChildren && styles.withChildren, ownerState.hasChildren && !ownerState.width && styles.fitContent, ownerState.hasChildren && !ownerState.height && styles.heightAuto];
  }
})((0, _memoTheme.default)(({
  theme
}) => {
  const radiusUnit = (0, _styles.unstable_getUnit)(theme.shape.borderRadius) || 'px';
  const radiusValue = (0, _styles.unstable_toUnitless)(theme.shape.borderRadius);
  return {
    display: 'block',
    // Create a "on paper" color with sufficient contrast retaining the color
    backgroundColor: theme.vars ? theme.vars.palette.Skeleton.bg : (0, _styles.alpha)(theme.palette.text.primary, theme.palette.mode === 'light' ? 0.11 : 0.13),
    height: '1.2em',
    variants: [{
      props: {
        variant: 'text'
      },
      style: {
        marginTop: 0,
        marginBottom: 0,
        height: 'auto',
        transformOrigin: '0 55%',
        transform: 'scale(1, 0.60)',
        borderRadius: `${radiusValue}${radiusUnit}/${Math.round(radiusValue / 0.6 * 10) / 10}${radiusUnit}`,
        '&:empty:before': {
          content: '"\\00a0"'
        }
      }
    }, {
      props: {
        variant: 'circular'
      },
      style: {
        borderRadius: '50%'
      }
    }, {
      props: {
        variant: 'rounded'
      },
      style: {
        borderRadius: (theme.vars || theme).shape.borderRadius
      }
    }, {
      props: ({
        ownerState
      }) => ownerState.hasChildren,
      style: {
        '& > *': {
          visibility: 'hidden'
        }
      }
    }, {
      props: ({
        ownerState
      }) => ownerState.hasChildren && !ownerState.width,
      style: {
        maxWidth: 'fit-content'
      }
    }, {
      props: ({
        ownerState
      }) => ownerState.hasChildren && !ownerState.height,
      style: {
        height: 'auto'
      }
    }, {
      props: {
        animation: 'pulse'
      },
      style: pulseAnimation || {
        animation: `${pulseKeyframe} 2s ease-in-out 0.5s infinite`
      }
    }, {
      props: {
        animation: 'wave'
      },
      style: {
        position: 'relative',
        overflow: 'hidden',
        /* Fix bug in Safari https://bugs.webkit.org/show_bug.cgi?id=68196 */
        WebkitMaskImage: '-webkit-radial-gradient(white, black)',
        '&::after': {
          background: `linear-gradient(
                90deg,
                transparent,
                ${(theme.vars || theme).palette.action.hover},
                transparent
              )`,
          content: '""',
          position: 'absolute',
          transform: 'translateX(-100%)' /* Avoid flash during server-side hydration */,
          bottom: 0,
          left: 0,
          right: 0,
          top: 0
        }
      }
    }, {
      props: {
        animation: 'wave'
      },
      style: waveAnimation || {
        '&::after': {
          animation: `${waveKeyframe} 2s linear 0.5s infinite`
        }
      }
    }]
  };
}));
const Skeleton = /*#__PURE__*/React.forwardRef(function Skeleton(inProps, ref) {
  const props = (0, _DefaultPropsProvider.useDefaultProps)({
    props: inProps,
    name: 'MuiSkeleton'
  });
  const {
    animation = 'pulse',
    className,
    component = 'span',
    height,
    style,
    variant = 'text',
    width,
    ...other
  } = props;
  const ownerState = {
    ...props,
    animation,
    component,
    variant,
    hasChildren: Boolean(other.children)
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(SkeletonRoot, {
    as: component,
    ref: ref,
    className: (0, _clsx.default)(classes.root, className),
    ownerState: ownerState,
    ...other,
    style: {
      width,
      height,
      ...style
    }
  });
});
process.env.NODE_ENV !== "production" ? Skeleton.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The animation.
   * If `false` the animation effect is disabled.
   * @default 'pulse'
   */
  animation: _propTypes.default.oneOf(['pulse', 'wave', false]),
  /**
   * Optional children to infer width and height from.
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
   * Height of the skeleton.
   * Useful when you don't want to adapt the skeleton to a text element but for instance a card.
   */
  height: _propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.string]),
  /**
   * @ignore
   */
  style: _propTypes.default.object,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: _propTypes.default.oneOfType([_propTypes.default.arrayOf(_propTypes.default.oneOfType([_propTypes.default.func, _propTypes.default.object, _propTypes.default.bool])), _propTypes.default.func, _propTypes.default.object]),
  /**
   * The type of content that will be rendered.
   * @default 'text'
   */
  variant: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_propTypes.default.oneOf(['circular', 'rectangular', 'rounded', 'text']), _propTypes.default.string]),
  /**
   * Width of the skeleton.
   * Useful when the skeleton is inside an inline element with no width of its own.
   */
  width: _propTypes.default.oneOfType([_propTypes.default.number, _propTypes.default.string])
} : void 0;
var _default = exports.default = Skeleton;