require('es6-promise').polyfill()

global.Rollbar = {
  critical: (message, _callback) => {
    console.log(`Test mode, threw this error: ${message}`)
  },
  error: (message, _callback) => {
    console.log(`Test mode, threw this error: ${message}`)
  },
  warning: (message, _callback) => {
    console.log(`Test mode, threw this error: ${message}`)
  },
  info: (message, _callback) => {
    console.log(`Test mode, threw this error: ${message}`)
  },
  debug: (message, _callback) => {
    console.log(`Test mode, threw this error: ${message}`)
  }
}

global.__NODE_ENV__ = 'test'
global.fetch = () => {
  console.log('fetch is mocked')
}
