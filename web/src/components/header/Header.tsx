// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { AppBar, Toolbar } from '@mui/material'
import { DRAWER_WIDTH } from '../../helpers/theme'
import { createTheme } from '@mui/material/styles'
import { useTheme } from '@emotion/react'
import Box from '@mui/material/Box'
import React, { ReactElement } from 'react'
import Search from '../search/Search'

const Header = (): ReactElement => {
  const theme = createTheme(useTheme())

  return (
    <AppBar
      position='fixed'
      elevation={0}
      sx={{
        zIndex: theme.zIndex.drawer + 1,
        backgroundColor: theme.palette.background.default,
        borderBottom: `2px dashed ${theme.palette.secondary.main}`,
        left: `${DRAWER_WIDTH + 1}px`,
      }}
    >
      <Toolbar disableGutters>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            width: 'calc(100% - 97px)',
          }}
        >
          <Box display={'flex'} alignItems={'center'}>
            <Search />
          </Box>
        </Box>
      </Toolbar>
    </AppBar>
  )
}

export default Header
