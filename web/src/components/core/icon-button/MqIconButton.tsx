// SPDX-License-Identifier: Apache-2.0

import React, { ReactElement } from 'react'
import classNames from 'classnames'

import { Theme } from '@material-ui/core'
import { theme } from '../../../helpers/theme'
import Box from '@material-ui/core/Box'
import ButtonBase from '@material-ui/core/ButtonBase'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) =>
  createStyles({
    root: {
      padding: theme.spacing(4),
      width: theme.spacing(8),
      height: theme.spacing(8),
      borderRadius: theme.spacing(2),
      color: theme.palette.secondary.main,
      background: theme.palette.background.default,
      transition: theme.transitions.create(['background-color', 'color']),
      border: '2px solid transparent',
      '&:hover': {
        border: `2px dashed ${theme.palette.primary.main}`
      }
    },
    active: {
      background: theme.palette.primary.main,
      color: theme.palette.common.white
    },
    iconButtonContainer: {
      color: 'transparent',
      transition: theme.transitions.create(['color']),
      '&:hover': {
        color: theme.palette.primary.main
      }
    },
    tooltip: {
      fontFamily: 'Karla',
      userSelect: 'none'
    }
  })

interface OwnProps {
  id: string
  title: string
  children: ReactElement
  active: boolean
}

type IconButtonProps = WithStyles<typeof styles> & OwnProps

class MqIconButton extends React.Component<IconButtonProps> {
  render() {
    const { id, classes, title, active } = this.props
    return (
      <Box className={classes.iconButtonContainer}>
        <ButtonBase
          id={id}
          disableRipple={true}
          className={classNames(classes.root, active && classes.active)}
        >
          {this.props.children}
        </ButtonBase>
        <Box
          display={'flex'}
          justifyContent={'center'}
          width={theme.spacing(8)}
          className={classes.tooltip}
        >
          {title}
        </Box>
      </Box>
    )
  }
}

export default withStyles(styles)(MqIconButton)
