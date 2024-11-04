"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
exports.getPopperUtilityClass = getPopperUtilityClass;
var _generateUtilityClasses = _interopRequireDefault(require("@mui/utils/generateUtilityClasses"));
var _generateUtilityClass = _interopRequireDefault(require("@mui/utils/generateUtilityClass"));
function getPopperUtilityClass(slot) {
  return (0, _generateUtilityClass.default)('MuiPopper', slot);
}
const popperClasses = (0, _generateUtilityClasses.default)('MuiPopper', ['root']);
var _default = exports.default = popperClasses;