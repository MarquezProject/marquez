// SPDX-License-Identifier: Apache-2.0

import { THEME_EXTRA } from '../../../helpers/theme'
import { Theme, alpha } from '@material-ui/core/styles'
import Box from '@material-ui/core/Box'
import MqText from '../text/MqText'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) =>
  createStyles({
    codeContainer: {
      padding: `${theme.spacing(2)}px ${theme.spacing(4)}px`,
      backgroundColor: alpha(theme.palette.common.white, 0.1),
      borderLeft: `2px dashed ${THEME_EXTRA.typography.subdued}`,
      whiteSpace: 'pre-wrap'
    }
  })

interface OwnProps {
  code?: string
  description?: string
}

const MqCode: React.FC<OwnProps & WithStyles<typeof styles>> = ({ code, description, classes }) => {
  return (
    <Box className={classes.codeContainer}>
      {description && (
        <Box mb={2}>
          <MqText bold font={'mono'} subdued>
            {description}
          </MqText>
        </Box>
      )}
      <MqText font={'mono'} subdued>
        {code ? code : 'Nothing to show here'}
      </MqText>
    </Box>
  )
}

export default withStyles(styles)(MqCode)
