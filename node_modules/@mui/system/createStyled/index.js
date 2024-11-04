"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {};
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _createStyled.default;
  }
});
var _createStyled = _interopRequireWildcard(require("./createStyled"));
Object.keys(_createStyled).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _createStyled[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _createStyled[key];
    }
  });
});