"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
exports.useStepContext = useStepContext;
var React = _interopRequireWildcard(require("react"));
/**
 * Provides information about the current step in Stepper.
 */
const StepContext = /*#__PURE__*/React.createContext({});
if (process.env.NODE_ENV !== 'production') {
  StepContext.displayName = 'StepContext';
}

/**
 * Returns the current StepContext or an empty object if no StepContext
 * has been defined in the component tree.
 */
function useStepContext() {
  return React.useContext(StepContext);
}
var _default = exports.default = StepContext;