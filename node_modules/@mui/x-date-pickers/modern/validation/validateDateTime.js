import { validateDate } from "./validateDate.js";
import { validateTime } from "./validateTime.js";
import { singleItemValueManager } from "../internals/utils/valueManagers.js";
export const validateDateTime = ({
  adapter,
  value,
  timezone,
  props
}) => {
  const dateValidationResult = validateDate({
    adapter,
    value,
    timezone,
    props
  });
  if (dateValidationResult !== null) {
    return dateValidationResult;
  }
  return validateTime({
    adapter,
    value,
    timezone,
    props
  });
};
validateDateTime.valueManager = singleItemValueManager;