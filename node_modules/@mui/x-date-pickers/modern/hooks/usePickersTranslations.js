'use client';

import { useLocalizationContext } from "../internals/hooks/useUtils.js";
export const usePickersTranslations = () => useLocalizationContext().localeText;