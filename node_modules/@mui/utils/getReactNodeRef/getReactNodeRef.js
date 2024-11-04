"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = getReactNodeRef;
var React = _interopRequireWildcard(require("react"));
/**
 * Returns the ref of a React node handling differences between React 19 and older versions.
 * It will return null if the node is not a valid React element.
 *
 * @param element React.ReactNode
 * @returns React.Ref<any> | null
 *
 * @deprecated Use getReactElementRef instead
 */
function getReactNodeRef(element) {
  if (!element || ! /*#__PURE__*/React.isValidElement(element)) {
    return null;
  }

  // 'ref' is passed as prop in React 19, whereas 'ref' is directly attached to children in older versions
  return element.props.propertyIsEnumerable('ref') ? element.props.ref :
  // @ts-expect-error element.ref is not included in the ReactElement type
  // We cannot check for it, but isValidElement is true at this point
  // https://github.com/DefinitelyTyped/DefinitelyTyped/discussions/70189
  element.ref;
}