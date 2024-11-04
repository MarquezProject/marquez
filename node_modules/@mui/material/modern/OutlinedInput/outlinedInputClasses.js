import generateUtilityClasses from '@mui/utils/generateUtilityClasses';
import generateUtilityClass from '@mui/utils/generateUtilityClass';
import { inputBaseClasses } from "../InputBase/index.js";
export function getOutlinedInputUtilityClass(slot) {
  return generateUtilityClass('MuiOutlinedInput', slot);
}
const outlinedInputClasses = {
  ...inputBaseClasses,
  ...generateUtilityClasses('MuiOutlinedInput', ['root', 'notchedOutline', 'input'])
};
export default outlinedInputClasses;