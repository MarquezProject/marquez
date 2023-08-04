// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import { createRoot } from 'react-dom/client'
import App from './components/App'

// fonts
import './index.css'

import './i18n/config'

const container = document.getElementById('root')
if (container) {
  const root = createRoot(container)
  root.render(<App />)
}
