// SPDX-License-Identifier: Apache-2.0

import { API_DOCS_URL } from '../../globals'
import { AppBar, Toolbar } from '@material-ui/core'
import { DRAWER_WIDTH } from '../../helpers/theme'
import { Link } from 'react-router-dom'
import { Theme, WithStyles, createStyles, withStyles } from '@material-ui/core/styles'
import Box from '@material-ui/core/Box'
import MqText from '../core/text/MqText'
import NamespaceSelect from '../namespace-select/NamespaceSelect'
import React, { ReactElement } from 'react'
import Search from '../search/Search'

const styles = (theme: Theme) => {
  return createStyles({
    appBar: {
      zIndex: theme.zIndex.drawer + 1,
      backgroundColor: theme.palette.background.default,
      borderBottom: `2px dashed ${theme.palette.secondary.main}`,
      padding: `${theme.spacing(2)}px 0`,
      left: DRAWER_WIDTH + 1,
      width: `calc(100% - ${DRAWER_WIDTH}px)`
    },
    toolbar: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    },
    innerToolbar: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      maxWidth: theme.breakpoints.values.lg,
      width: theme.breakpoints.values.lg,
      margin: '0 auto'
    }
  })
}

type HeaderProps = WithStyles<typeof styles>

const Header = (props: HeaderProps): ReactElement => {
  const { classes } = props
  return (
    <AppBar position='fixed' elevation={0} className={classes.appBar}>
      <Toolbar>
        <Box className={classes.innerToolbar}>
          <Link to='/'>
            <img src={require('../../img/marquez_logo.svg')} height={48} alt='Marquez Logo' />
          </Link>
          <Box display={'flex'} alignItems={'center'}>
            <Search />
            <NamespaceSelect />
            <Box ml={2}>
              <MqText link href={API_DOCS_URL}>
                API Docs
              </MqText>
            </Box>
          </Box>
        </Box>
      </Toolbar>
    </AppBar>
  )
}

export default withStyles(styles)(Header)
