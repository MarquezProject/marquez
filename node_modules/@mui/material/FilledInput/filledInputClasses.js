import generateUtilityClasses from '@mui/utils/generateUtilityClasses';
import generateUtilityClass from '@mui/utils/generateUtilityClass';
import { inputBaseClasses } from "../InputBase/index.js";
export function getFilledInputUtilityClass(slot) {
  return generateUtilityClass('MuiFilledInput', slot);
}
const filledInputClasses = {
  ...inputBaseClasses,
  ...generateUtilityClasses('MuiFilledInput', ['root', 'underline', 'input', 'adornedStart', 'adornedEnd', 'sizeSmall', 'multiline', 'hiddenLabel'])
};
export default filledInputClasses;