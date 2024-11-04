"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {};
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _positions.default;
  }
});
var _positions = _interopRequireWildcard(require("./positions"));
Object.keys(_positions).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _positions[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _positions[key];
    }
  });
});