'use client';

import * as React from 'react';
import RadioGroupContext from "./RadioGroupContext.js";
export default function useRadioGroup() {
  return React.useContext(RadioGroupContext);
}