'use client';

import * as React from 'react';
import FormControlContext from "./FormControlContext.js";
export default function useFormControl() {
  return React.useContext(FormControlContext);
}