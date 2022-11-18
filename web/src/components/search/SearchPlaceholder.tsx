// SPDX-License-Identifier: Apache-2.0

import { Box, Theme, createStyles } from '@material-ui/core'
import { theme } from '../../helpers/theme'
import MqText from '../core/text/MqText'
import React from 'react'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'
import '../../i18n/config'
import { useTranslation } from 'react-i18next'

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
  const { t } = useTranslation();
  return (
    <Box className={classes.root}>
      <Box display={'inline'}>
        <MqText disabled inline>
          {' '}
          {t('search.search')}
        </MqText>{' '}
        <MqText bold inline font={'mono'} color={theme.palette.common.white}>
          {' '}
          {t('search.jobs')}
        </MqText>{' '}
        <MqText disabled inline>
          {' '}
          {t('search.and')}
        </MqText>{' '}
        <MqText bold inline font={'mono'} color={theme.palette.common.white}>
          {' '}
          {t('search.datasets')}
        </MqText>
      </Box>
    </Box>
  )
}

export default withStyles(styles)(SearchPlaceholder)
