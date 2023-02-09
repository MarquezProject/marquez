// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box, Theme, createStyles } from '@material-ui/core'
import { theme } from '../../helpers/theme'
import MqText from '../core/text/MqText'
import React from 'react'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) =>
  createStyles({
    root: {
      zIndex: theme.zIndex.appBar + 3,
      position: 'absolute',
      top: 8,
      left: 122,
      height: 0,
      overflow: 'visible',
      pointerEvents: 'none'
    }
  })

const importI18next = () => {
  const i18next = require('i18next')
  return i18next
}

const SearchPlaceholder: React.FC<WithStyles<typeof styles>> = ({ classes }) => {
  const i18next = importI18next()
  return (
    <Box className={classes.root}>
      <Box display={'inline'}>
        <MqText disabled inline>
          {' '}
          {i18next.t('search.search')}
        </MqText>{' '}
        <MqText bold inline font={'mono'} color={theme.palette.common.white}>
          {' '}
          {i18next.t('search.jobs')}
        </MqText>{' '}
        <MqText disabled inline>
          {' '}
          {i18next.t('search.and')}
        </MqText>{' '}
        <MqText bold inline font={'mono'} color={theme.palette.common.white}>
          {' '}
          {i18next.t('search.datasets')}
        </MqText>
      </Box>
    </Box>
  )
}

export default withStyles(styles)(SearchPlaceholder)
