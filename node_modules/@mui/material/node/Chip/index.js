"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {
  chipClasses: true
};
Object.defineProperty(exports, "chipClasses", {
  enumerable: true,
  get: function () {
    return _chipClasses.default;
  }
});
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _Chip.default;
  }
});
var _Chip = _interopRequireDefault(require("./Chip"));
var _chipClasses = _interopRequireWildcard(require("./chipClasses"));
Object.keys(_chipClasses).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _chipClasses[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _chipClasses[key];
    }
  });
});