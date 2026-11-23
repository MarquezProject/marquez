// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

const { TextDecoder, TextEncoder } = require('util')

require('es6-promise').polyfill()

// React Router 7 uses the Encoding API at import time. jsdom in this Jest
// version does not install those globals.
global.TextEncoder = TextEncoder
global.TextDecoder = TextDecoder

global.__NODE_ENV__ = 'test'
global.fetch = () => {
  console.log('fetch is mocked')
}
