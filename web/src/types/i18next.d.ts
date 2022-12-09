import { resources } from '../i18n/config'

declare module 'i18next' {
  interface CustomTypeOptions {
    translation: typeof translation
    resources: {
      en: typeof resources['en']
      es: typeof resources['es']
      fr: typeof resources['fr']
      pl: typeof resources['pl']
    }
    allowObjectInHTMLChildren: true
  }
}

export default resources
