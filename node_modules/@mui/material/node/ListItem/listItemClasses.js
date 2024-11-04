"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
exports.getListItemUtilityClass = getListItemUtilityClass;
var _generateUtilityClasses = _interopRequireDefault(require("@mui/utils/generateUtilityClasses"));
var _generateUtilityClass = _interopRequireDefault(require("@mui/utils/generateUtilityClass"));
function getListItemUtilityClass(slot) {
  return (0, _generateUtilityClass.default)('MuiListItem', slot);
}
const listItemClasses = (0, _generateUtilityClasses.default)('MuiListItem', ['root', 'container', 'dense', 'alignItemsFlexStart', 'divider', 'gutters', 'padding', 'secondaryAction']);
var _default = exports.default = listItemClasses;