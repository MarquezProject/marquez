"use strict";
'use client';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.usePickersTranslations = void 0;
var _useUtils = require("../internals/hooks/useUtils");
const usePickersTranslations = () => (0, _useUtils.useLocalizationContext)().localeText;
exports.usePickersTranslations = usePickersTranslations;