"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {
  grid2Classes: true
};
Object.defineProperty(exports, "default", {
  enumerable: true,
  get: function () {
    return _PigmentGrid.default;
  }
});
Object.defineProperty(exports, "grid2Classes", {
  enumerable: true,
  get: function () {
    return _grid2Classes.default;
  }
});
var _PigmentGrid = _interopRequireWildcard(require("./PigmentGrid"));
Object.keys(_PigmentGrid).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _PigmentGrid[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _PigmentGrid[key];
    }
  });
});
var _grid2Classes = _interopRequireDefault(require("../Grid2/grid2Classes"));