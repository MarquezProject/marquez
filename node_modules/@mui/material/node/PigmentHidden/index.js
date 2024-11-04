"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {};
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _PigmentHidden.default;
  }
});
var _PigmentHidden = _interopRequireWildcard(require("./PigmentHidden"));
Object.keys(_PigmentHidden).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _PigmentHidden[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _PigmentHidden[key];
    }
  });
});