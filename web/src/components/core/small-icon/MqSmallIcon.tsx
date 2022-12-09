// SPDX-License-Identifier: Apache-2.0

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IconDefinition } from '@fortawesome/free-solid-svg-icons'
import Box from '@material-ui/core/Box'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = () =>
  createStyles({
    icon: {
      width: '10px !important',
      height: 9,
      fontSize: 9
    }
  })

interface OwnProps {
  icon: IconDefinition
  backgroundColor: string
  foregroundColor: string
  shape: 'circle' | 'rect'
}

const MqSmallIcon: React.FC<OwnProps & WithStyles<typeof styles>> = ({
  icon,
  backgroundColor,
  foregroundColor,
  shape,
  classes
}) => {
  return (
    <Box
      width={16}
      height={16}
      bgcolor={backgroundColor}
      borderRadius={shape === 'circle' ? '50%' : '4px'}
      display={'flex'}
      justifyContent={'center'}
      alignItems={'center'}
    >
      <FontAwesomeIcon className={classes.icon} fixedWidth icon={icon} color={foregroundColor} />
    </Box>
  )
}

export default withStyles(styles)(MqSmallIcon)
