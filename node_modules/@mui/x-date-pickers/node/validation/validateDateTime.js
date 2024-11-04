"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.validateDateTime = void 0;
var _validateDate = require("./validateDate");
var _validateTime = require("./validateTime");
var _valueManagers = require("../internals/utils/valueManagers");
const validateDateTime = ({
  adapter,
  value,
  timezone,
  props
}) => {
  const dateValidationResult = (0, _validateDate.validateDate)({
    adapter,
    value,
    timezone,
    props
  });
  if (dateValidationResult !== null) {
    return dateValidationResult;
  }
  return (0, _validateTime.validateTime)({
    adapter,
    value,
    timezone,
    props
  });
};
exports.validateDateTime = validateDateTime;
validateDateTime.valueManager = _valueManagers.singleItemValueManager;