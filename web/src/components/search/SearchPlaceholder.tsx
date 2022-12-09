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

const SearchPlaceholder: React.FC<WithStyles<typeof styles>> = ({ classes }) => {
  return (
    <Box className={classes.root}>
      <Box display={'inline'}>
        <MqText disabled inline>
          {' '}
          Search
        </MqText>{' '}
        <MqText bold inline font={'mono'} color={theme.palette.common.white}>
          {' '}
          Jobs
        </MqText>{' '}
        <MqText disabled inline>
          {' '}
          and
        </MqText>{' '}
        <MqText bold inline font={'mono'} color={theme.palette.common.white}>
          {' '}
          Datasets
        </MqText>
      </Box>
    </Box>
  )
}

export default withStyles(styles)(SearchPlaceholder)
