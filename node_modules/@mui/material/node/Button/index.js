"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {
  buttonClasses: true
};
Object.defineProperty(exports, "buttonClasses", {
  enumerable: true,
  get: function () {
    return _buttonClasses.default;
  }
});
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _Button.default;
  }
});
var _Button = _interopRequireDefault(require("./Button"));
var _buttonClasses = _interopRequireWildcard(require("./buttonClasses"));
Object.keys(_buttonClasses).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _buttonClasses[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _buttonClasses[key];
    }
  });
});