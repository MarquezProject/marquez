// SPDX-License-Identifier: Apache-2.0

import { Box, Theme, createStyles } from '@material-ui/core'
import MqText from '../text/MqText'
import React, { ReactElement } from 'react'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

interface MqEmptyProps {
  emoji?: string
  title?: string
  body?: string
  children?: ReactElement
}

const styles = (theme: Theme) =>
  createStyles({
    root: {
      display: 'flex',
      justifyContent: 'center'
    },
    inner: {
      padding: theme.spacing(2),
      border: `2px dashed ${theme.palette.secondary.main}`,
      borderRadius: theme.shape.borderRadius,
      width: '400px'
    },
    icon: {
      fontSize: theme.spacing(7),
      height: theme.spacing(7),
      lineHeight: `${theme.spacing(7)}px`
    }
  })

const MqEmpty: React.FC<MqEmptyProps & WithStyles<typeof styles>> = ({
  classes,
  title,
  body,
  emoji,
  children
}) => {
  return (
    <Box className={classes.root}>
      <Box className={classes.inner}>
        {title && (
          <Box mb={1}>
            <MqText heading>{title}</MqText>
          </Box>
        )}
        {body && (
          <Box>
            <MqText inline>{body}</MqText>
          </Box>
        )}
        {children && <Box>{children}</Box>}
        {emoji && (
          <Box className={classes.icon} mt={1}>
            <span role={'img'} aria-label={'icon'}>
              {emoji}
            </span>
          </Box>
        )}
      </Box>
    </Box>
  )
}

export default withStyles(styles)(MqEmpty)
