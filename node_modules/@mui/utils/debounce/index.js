"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {};
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _debounce.default;
  }
});
var _debounce = _interopRequireWildcard(require("./debounce"));
Object.keys(_debounce).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _debounce[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _debounce[key];
    }
  });
});