"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {
  stackClasses: true
};
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _PigmentStack.default;
  }
});
Object.defineProperty(exports, "stackClasses", {
  enumerable: true,
  get: function () {
    return _stackClasses.default;
  }
});
var _PigmentStack = _interopRequireWildcard(require("./PigmentStack"));
Object.keys(_PigmentStack).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _PigmentStack[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _PigmentStack[key];
    }
  });
});
var _stackClasses = _interopRequireDefault(require("../Stack/stackClasses"));