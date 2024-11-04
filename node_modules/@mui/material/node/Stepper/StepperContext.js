"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
exports.useStepperContext = useStepperContext;
var React = _interopRequireWildcard(require("react"));
/**
 * Provides information about the current step in Stepper.
 */
const StepperContext = /*#__PURE__*/React.createContext({});
if (process.env.NODE_ENV !== 'production') {
  StepperContext.displayName = 'StepperContext';
}

/**
 * Returns the current StepperContext or an empty object if no StepperContext
 * has been defined in the component tree.
 */
function useStepperContext() {
  return React.useContext(StepperContext);
}
var _default = exports.default = StepperContext;