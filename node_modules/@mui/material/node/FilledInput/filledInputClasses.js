"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
exports.getFilledInputUtilityClass = getFilledInputUtilityClass;
var _generateUtilityClasses = _interopRequireDefault(require("@mui/utils/generateUtilityClasses"));
var _generateUtilityClass = _interopRequireDefault(require("@mui/utils/generateUtilityClass"));
var _InputBase = require("../InputBase");
function getFilledInputUtilityClass(slot) {
  return (0, _generateUtilityClass.default)('MuiFilledInput', slot);
}
const filledInputClasses = {
  ..._InputBase.inputBaseClasses,
  ...(0, _generateUtilityClasses.default)('MuiFilledInput', ['root', 'underline', 'input', 'adornedStart', 'adornedEnd', 'sizeSmall', 'multiline', 'hiddenLabel'])
};
var _default = exports.default = filledInputClasses;