"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {};
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _useIsFocusVisible.default;
  }
});
var _useIsFocusVisible = _interopRequireWildcard(require("./useIsFocusVisible"));
Object.keys(_useIsFocusVisible).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _useIsFocusVisible[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _useIsFocusVisible[key];
    }
  });
});