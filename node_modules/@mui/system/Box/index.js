"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {
  boxClasses: true
};
Object.defineProperty(exports, "boxClasses", {
  enumerable: true,
  get: function () {
    return _boxClasses.default;
  }
});
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _Box.default;
  }
});
var _Box = _interopRequireDefault(require("./Box"));
var _boxClasses = _interopRequireWildcard(require("./boxClasses"));
Object.keys(_boxClasses).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _boxClasses[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _boxClasses[key];
    }
  });
});