// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { defaultNS, resources } from '../i18n/config'

declare module 'i18next' {
  interface CustomTypeOptions {
    defaultNS: typeof defaultNS
    translation: typeof translation
    resources: (typeof resources)['en']
    allowObjectInHTMLChildren: true
  }
}

export default resources
