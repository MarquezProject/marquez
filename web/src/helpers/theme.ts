// SPDX-License-Identifier: Apache-2.0

import { createTheme } from '@material-ui/core'

export const theme = createTheme({
  overrides: {
    MuiCssBaseline: {
      '@global': {
        body: {
          color: '#fff'
        }
      }
    }
  },
  typography: {
    fontFamily: 'Karla',
    h1: {
      fontSize: '2.5rem',
      fontWeight: 600
    },
    h3: {
      fontSize: '1rem',
      fontWeight: 600
    },
    fontSize: 14
  },
  palette: {
    type: 'dark',
    primary: {
      main: '#71ddbf'
    },
    error: {
      main: '#ee7b7b'
    },
    background: {
      default: '#191f26'
    },
    secondary: {
      main: '#454f5b'
    }
  }
})

export const THEME_EXTRA = {
  typography: {
    subdued: '#abb1bd',
    disabled: '#8d9499'
  }
}

// 1px for bottom border
export const HEADER_HEIGHT = 96 + 1
export const DRAWER_WIDTH = 96
