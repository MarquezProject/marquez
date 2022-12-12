import i18next from '../i18n/config'
import { resources, defaultNS } from '../i18n/config'

declare module 'i18next' {
  interface CustomTypeOptions {
    defaultNS: typeof defaultNS
    translation: typeof translation
    resources: typeof resources['en']
    allowObjectInHTMLChildren: true
  }
}

export default resources
