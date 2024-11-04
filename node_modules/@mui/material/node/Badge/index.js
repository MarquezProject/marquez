"use strict";

var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {
  badgeClasses: true
};
Object.defineProperty(exports, "badgeClasses", {
  enumerable: true,
  get: function () {
    return _badgeClasses.default;
  }
});
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _Badge.default;
  }
});
var _Badge = _interopRequireDefault(require("./Badge"));
var _badgeClasses = _interopRequireWildcard(require("./badgeClasses"));
Object.keys(_badgeClasses).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _badgeClasses[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _badgeClasses[key];
    }
  });
});