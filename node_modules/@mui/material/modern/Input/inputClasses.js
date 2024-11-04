import generateUtilityClasses from '@mui/utils/generateUtilityClasses';
import generateUtilityClass from '@mui/utils/generateUtilityClass';
import { inputBaseClasses } from "../InputBase/index.js";
export function getInputUtilityClass(slot) {
  return generateUtilityClass('MuiInput', slot);
}
const inputClasses = {
  ...inputBaseClasses,
  ...generateUtilityClasses('MuiInput', ['root', 'underline', 'input'])
};
export default inputClasses;