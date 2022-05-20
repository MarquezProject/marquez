// Copyright 2018-2022 contributors to the Marquez project

require('es6-promise').polyfill()

global.__NODE_ENV__ = 'test'
global.fetch = () => {
  console.log('fetch is mocked')
}
