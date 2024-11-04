"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = propsToClassKey;
var _capitalize = _interopRequireDefault(require("@mui/utils/capitalize"));
function isEmpty(string) {
  return string.length === 0;
}

/**
 * Generates string classKey based on the properties provided. It starts with the
 * variant if defined, and then it appends all other properties in alphabetical order.
 * @param {object} props - the properties for which the classKey should be created.
 */
function propsToClassKey(props) {
  const {
    variant,
    ...other
  } = props;
  let classKey = variant || '';
  Object.keys(other).sort().forEach(key => {
    if (key === 'color') {
      classKey += isEmpty(classKey) ? props[key] : (0, _capitalize.default)(props[key]);
    } else {
      classKey += `${isEmpty(classKey) ? key : (0, _capitalize.default)(key)}${(0, _capitalize.default)(props[key].toString())}`;
    }
  });
  return classKey;
}