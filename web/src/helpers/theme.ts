import { createMuiTheme } from '@material-ui/core/styles'

export const theme = createMuiTheme({
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
      main: '#3587e8'
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
