"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var React = _interopRequireWildcard(require("react"));
var ReactDOM = _interopRequireWildcard(require("react-dom"));
var _propTypes = _interopRequireDefault(require("prop-types"));
var _utils = require("@mui/utils");
var _jsxRuntime = require("react/jsx-runtime");
function getContainer(container) {
  return typeof container === 'function' ? container() : container;
}

/**
 * Portals provide a first-class way to render children into a DOM node
 * that exists outside the DOM hierarchy of the parent component.
 *
 * Demos:
 *
 * - [Portal](https://mui.com/material-ui/react-portal/)
 *
 * API:
 *
 * - [Portal API](https://mui.com/material-ui/api/portal/)
 */
const Portal = /*#__PURE__*/React.forwardRef(function Portal(props, forwardedRef) {
  const {
    children,
    container,
    disablePortal = false
  } = props;
  const [mountNode, setMountNode] = React.useState(null);
  const handleRef = (0, _utils.unstable_useForkRef)(/*#__PURE__*/React.isValidElement(children) ? (0, _utils.unstable_getReactElementRef)(children) : null, forwardedRef);
  (0, _utils.unstable_useEnhancedEffect)(() => {
    if (!disablePortal) {
      setMountNode(getContainer(container) || document.body);
    }
  }, [container, disablePortal]);
  (0, _utils.unstable_useEnhancedEffect)(() => {
    if (mountNode && !disablePortal) {
      (0, _utils.unstable_setRef)(forwardedRef, mountNode);
      return () => {
        (0, _utils.unstable_setRef)(forwardedRef, null);
      };
    }
    return undefined;
  }, [forwardedRef, mountNode, disablePortal]);
  if (disablePortal) {
    if (/*#__PURE__*/React.isValidElement(children)) {
      const newProps = {
        ref: handleRef
      };
      return /*#__PURE__*/React.cloneElement(children, newProps);
    }
    return /*#__PURE__*/(0, _jsxRuntime.jsx)(React.Fragment, {
      children: children
    });
  }
  return /*#__PURE__*/(0, _jsxRuntime.jsx)(React.Fragment, {
    children: mountNode ? /*#__PURE__*/ReactDOM.createPortal(children, mountNode) : mountNode
  });
});
process.env.NODE_ENV !== "production" ? Portal.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │ To update them, edit the TypeScript types and run `pnpm proptypes`. │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * The children to render into the `container`.
   */
  children: _propTypes.default.node,
  /**
   * An HTML element or function that returns one.
   * The `container` will have the portal children appended to it.
   *
   * You can also provide a callback, which is called in a React layout effect.
   * This lets you set the container from a ref, and also makes server-side rendering possible.
   *
   * By default, it uses the body of the top-level document object,
   * so it's simply `document.body` most of the time.
   */
  container: _propTypes.default /* @typescript-to-proptypes-ignore */.oneOfType([_utils.HTMLElementType, _propTypes.default.func]),
  /**
   * The `children` will be under the DOM hierarchy of the parent component.
   * @default false
   */
  disablePortal: _propTypes.default.bool
} : void 0;
if (process.env.NODE_ENV !== 'production') {
  // eslint-disable-next-line
  Portal['propTypes' + ''] = (0, _utils.exactProp)(Portal.propTypes);
}
var _default = exports.default = Portal;