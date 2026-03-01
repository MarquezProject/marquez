// Copyright 2018-2026 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as ReactDOMClient from 'react-dom/client';
import App from './components/App'

// fonts
import './index.css'

import './i18n/config'

const container = document.getElementById('root');

const root = ReactDOMClient.createRoot(container);

root.render(<App />);
