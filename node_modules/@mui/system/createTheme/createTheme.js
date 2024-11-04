"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _deepmerge = _interopRequireDefault(require("@mui/utils/deepmerge"));
var _createBreakpoints = _interopRequireDefault(require("../createBreakpoints/createBreakpoints"));
var _cssContainerQueries = _interopRequireDefault(require("../cssContainerQueries"));
var _shape = _interopRequireDefault(require("./shape"));
var _createSpacing = _interopRequireDefault(require("./createSpacing"));
var _styleFunctionSx = _interopRequireDefault(require("../styleFunctionSx/styleFunctionSx"));
var _defaultSxConfig = _interopRequireDefault(require("../styleFunctionSx/defaultSxConfig"));
var _applyStyles = _interopRequireDefault(require("./applyStyles"));
function createTheme(options = {}, ...args) {
  const {
    breakpoints: breakpointsInput = {},
    palette: paletteInput = {},
    spacing: spacingInput,
    shape: shapeInput = {},
    ...other
  } = options;
  const breakpoints = (0, _createBreakpoints.default)(breakpointsInput);
  const spacing = (0, _createSpacing.default)(spacingInput);
  let muiTheme = (0, _deepmerge.default)({
    breakpoints,
    direction: 'ltr',
    components: {},
    // Inject component definitions.
    palette: {
      mode: 'light',
      ...paletteInput
    },
    spacing,
    shape: {
      ..._shape.default,
      ...shapeInput
    }
  }, other);
  muiTheme = (0, _cssContainerQueries.default)(muiTheme);
  muiTheme.applyStyles = _applyStyles.default;
  muiTheme = args.reduce((acc, argument) => (0, _deepmerge.default)(acc, argument), muiTheme);
  muiTheme.unstable_sxConfig = {
    ..._defaultSxConfig.default,
    ...other?.unstable_sxConfig
  };
  muiTheme.unstable_sx = function sx(props) {
    return (0, _styleFunctionSx.default)({
      sx: props,
      theme: this
    });
  };
  return muiTheme;
}
var _default = exports.default = createTheme;