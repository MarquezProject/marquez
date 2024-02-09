// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { API_DOCS_URL } from '../../globals'
import { AppBar, Toolbar } from '@mui/material'
import { DRAWER_WIDTH } from '../../helpers/theme'
import { Link } from 'react-router-dom'
import { createTheme } from '@mui/material/styles'
import { useTheme } from '@emotion/react'
import Box from '@mui/material/Box'
import MqText from '../core/text/MqText'
import NamespaceSelect from '../namespace-select/NamespaceSelect'
import React, { ReactElement } from 'react'
import Search from '../search/Search'
import marquez_logo from './marquez_logo.svg'

const Header = (): ReactElement => {
  const i18next = require('i18next')
  const theme = createTheme(useTheme())

  return (
    <AppBar
      position='fixed'
      elevation={0}
      sx={{
        zIndex: theme.zIndex.drawer + 1,
        backgroundColor: theme.palette.background.default,
        borderBottom: `2px dashed ${theme.palette.secondary.main}`,
        padding: `${theme.spacing(2)} 0`,
        left: `${DRAWER_WIDTH + 1}px`,
      }}
    >
      <Toolbar>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            width: 'calc(100% - 97px)',
          }}
        >
          <Link to='/'>
            <img src={marquez_logo} height={48} alt='Marquez Logo' />
          </Link>
          <Box display={'flex'} alignItems={'center'}>
            <Search />
            <NamespaceSelect />
            <Box ml={2}>
              <MqText link href={API_DOCS_URL}>
                {i18next.t('header.docs_link')}
              </MqText>
            </Box>
          </Box>
        </Box>
      </Toolbar>
    </AppBar>
  )
}

export default Header
