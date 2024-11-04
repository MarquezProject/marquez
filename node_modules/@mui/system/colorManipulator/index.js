"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var _colorManipulator = require("./colorManipulator");
Object.keys(_colorManipulator).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (key in exports && exports[key] === _colorManipulator[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _colorManipulator[key];
    }
  });
});