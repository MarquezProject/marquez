"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _isHostComponent = _interopRequireDefault(require("./isHostComponent"));
const shouldSpreadAdditionalProps = Slot => {
  return !Slot || !(0, _isHostComponent.default)(Slot);
};
var _default = exports.default = shouldSpreadAdditionalProps;