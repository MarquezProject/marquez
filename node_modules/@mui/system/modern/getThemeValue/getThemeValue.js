import borders from "../borders/index.js";
import display from "../display/index.js";
import flexbox from "../flexbox/index.js";
import grid from "../cssGrid/index.js";
import positions from "../positions/index.js";
import palette from "../palette/index.js";
import shadows from "../shadows/index.js";
import sizing from "../sizing/index.js";
import spacing from "../spacing/index.js";
import typography from "../typography/index.js";
const filterPropsMapping = {
  borders: borders.filterProps,
  display: display.filterProps,
  flexbox: flexbox.filterProps,
  grid: grid.filterProps,
  positions: positions.filterProps,
  palette: palette.filterProps,
  shadows: shadows.filterProps,
  sizing: sizing.filterProps,
  spacing: spacing.filterProps,
  typography: typography.filterProps
};
export const styleFunctionMapping = {
  borders,
  display,
  flexbox,
  grid,
  positions,
  palette,
  shadows,
  sizing,
  spacing,
  typography
};
export const propToStyleFunction = Object.keys(filterPropsMapping).reduce((acc, styleFnName) => {
  filterPropsMapping[styleFnName].forEach(propName => {
    acc[propName] = styleFunctionMapping[styleFnName];
  });
  return acc;
}, {});
function getThemeValue(prop, value, theme) {
  const inputProps = {
    [prop]: value,
    theme
  };
  const styleFunction = propToStyleFunction[prop];
  return styleFunction ? styleFunction(inputProps) : {
    [prop]: value
  };
}
export default getThemeValue;