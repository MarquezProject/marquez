//  from webpack
declare const __NODE_ENV__: string
declare const __DEVELOPMENT__: boolean

declare const __API_URL__: string

declare const __ROLLBAR__: boolean

declare const Rollbar: {
  critical: (message: any, callback?: any) => void
  error: (message: any, callback?: any) => void
  warning: (message: any, callback?: any) => void
  info: (message: any, callback?: any) => void
  debug: (message: any, callback?: any) => void
}

declare const __FEEDBACK_FORM_URL__: string
