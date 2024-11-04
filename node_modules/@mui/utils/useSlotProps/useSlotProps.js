"use strict";
'use client';

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _useForkRef = _interopRequireDefault(require("../useForkRef"));
var _appendOwnerState = _interopRequireDefault(require("../appendOwnerState"));
var _mergeSlotProps = _interopRequireDefault(require("../mergeSlotProps"));
var _resolveComponentProps = _interopRequireDefault(require("../resolveComponentProps"));
/**
 * @ignore - do not document.
 * Builds the props to be passed into the slot of an unstyled component.
 * It merges the internal props of the component with the ones supplied by the user, allowing to customize the behavior.
 * If the slot component is not a host component, it also merges in the `ownerState`.
 *
 * @param parameters.getSlotProps - A function that returns the props to be passed to the slot component.
 */
function useSlotProps(parameters) {
  const {
    elementType,
    externalSlotProps,
    ownerState,
    skipResolvingSlotProps = false,
    ...other
  } = parameters;
  const resolvedComponentsProps = skipResolvingSlotProps ? {} : (0, _resolveComponentProps.default)(externalSlotProps, ownerState);
  const {
    props: mergedProps,
    internalRef
  } = (0, _mergeSlotProps.default)({
    ...other,
    externalSlotProps: resolvedComponentsProps
  });
  const ref = (0, _useForkRef.default)(internalRef, resolvedComponentsProps?.ref, parameters.additionalProps?.ref);
  const props = (0, _appendOwnerState.default)(elementType, {
    ...mergedProps,
    ref
  }, ownerState);
  return props;
}
var _default = exports.default = useSlotProps;