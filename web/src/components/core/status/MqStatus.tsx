// SPDX-License-Identifier: Apache-2.0

import { theme } from '../../../helpers/theme'
import Box from '@material-ui/core/Box'
import MqText from '../text/MqText'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = () =>
  createStyles({
    type: {
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1)
    },
    status: {
      width: theme.spacing(2),
      height: theme.spacing(2),
      borderRadius: '50%'
    }
  })

interface OwnProps {
  color: string | null
  label?: string
}

const MqStatus: React.FC<OwnProps & WithStyles<typeof styles>> = ({ label, color, classes }) => {
  if (!color) {
    return null
  }
  return (
    <Box className={classes.type}>
      <Box className={classes.status} style={{ backgroundColor: color }} />
      {label && <MqText>{label}</MqText>}
    </Box>
  )
}

export default withStyles(styles)(MqStatus)
