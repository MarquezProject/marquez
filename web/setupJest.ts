// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

require('es6-promise').polyfill()

global.__NODE_ENV__ = 'test'
global.fetch = () => {
  console.log('fetch is mocked')
}
