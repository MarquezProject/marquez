// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { createTheme } from '@mui/material'

export const theme = createTheme({
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        '@global': {
          body: {
            color: '#fff',
          },
        },
        '.MuiInputBase-root': {
          paddingTop: '0',
          paddingBottom: '0',
        },
      },
    },
  },
  typography: {
    fontFamily: 'Karla',
    h1: {
      fontSize: '2.5rem',
      fontWeight: 600,
    },
    h3: {
      fontSize: '1rem',
      fontWeight: 600,
    },
    fontSize: 14,
  },
  palette: {
    mode: 'dark',
    primary: {
      main: '#71ddbf',
    },
    error: {
      main: '#ee7b7b',
    },
    warning: {
      main: '#7D7D7D',
    },
    info: {
      main: '#FECC00',
    },
    background: {
      default: '#191f26',
    },
    secondary: {
      main: '#454f5b',
    },
  },
})

export const THEME_EXTRA = {
  typography: {
    subdued: '#abb1bd',
    disabled: '#8d9499',
  },
}

// 1px for bottom border
export const HEADER_HEIGHT = 96 + 1
export const DRAWER_WIDTH = 96
