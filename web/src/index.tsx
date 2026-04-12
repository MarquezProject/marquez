// Copyright 2018-2026 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import * as ReactDOMClient from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './components/App';
import { ChakraProvider } from '@chakra-ui/react';

// fonts
import './index.css'

import './i18n/config'

const container = document.getElementById('root');

if (container) {
  const root = ReactDOMClient.createRoot(container);
  root.render(
    <BrowserRouter>
      <ChakraProvider>
        <App />
      </ChakraProvider>
    </BrowserRouter>
  );
}
