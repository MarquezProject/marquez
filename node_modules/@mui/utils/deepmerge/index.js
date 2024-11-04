"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {};
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _deepmerge.default;
  }
});
var _deepmerge = _interopRequireWildcard(require("./deepmerge"));
Object.keys(_deepmerge).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _deepmerge[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _deepmerge[key];
    }
  });
});