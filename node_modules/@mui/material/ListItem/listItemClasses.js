import generateUtilityClasses from '@mui/utils/generateUtilityClasses';
import generateUtilityClass from '@mui/utils/generateUtilityClass';
export function getListItemUtilityClass(slot) {
  return generateUtilityClass('MuiListItem', slot);
}
const listItemClasses = generateUtilityClasses('MuiListItem', ['root', 'container', 'dense', 'alignItemsFlexStart', 'divider', 'gutters', 'padding', 'secondaryAction']);
export default listItemClasses;